package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.config.PointsConfig;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRule;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.marketing.PointsRecordMapper;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRuleService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements PointsRecordService {

    private final UserService userService;
    private final PointsConfig pointsConfig;
    private final PointsRuleService pointsRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer points, String type, String description, Long relatedId) {
        addPointsWithExpiry(userId, points, type, description, relatedId, pointsConfig.getExpireDays());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPointsWithExpiry(Long userId, Integer points, String type, String description, Long relatedId, int expireDays) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int newBalance = user.getPoints() + points;
        user.setPoints(newBalance);
        userService.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setBalanceAfter(newBalance);
        record.setType(type);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        record.setExpireTime(LocalDateTime.now().plusDays(expireDays));
        record.setIsExpired(false);
        this.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInResult signIn(Long userId) {
        PointsRule signInRule = getSignInRule();
        int dailyLimit = signInRule != null && signInRule.getDailyLimit() != null ? signInRule.getDailyLimit() : 1;
        
        if (dailyLimit != 0) {
            int todayCount = getTodaySignInCount(userId);
            if (todayCount >= dailyLimit) {
                throw new RuntimeException("今日已签到" + todayCount + "次，已达上限");
            }
        }
        
        int signInPoints = calculateSignInPoints(signInRule);
        int consecutiveDays = getConsecutiveSignInDays(userId);
        int bonusPoints = calculateConsecutiveBonus(consecutiveDays, signInRule);
        int totalPoints = signInPoints + bonusPoints;
        
        log.info("用户签到 - userId: {}, 签到积分: {}, 连续天数: {}, 奖励积分: {}, 总积分: {}, 每日上限: {}", 
                userId, totalPoints, consecutiveDays + 1, bonusPoints, totalPoints, dailyLimit == 0 ? "不限" : dailyLimit);
        
        String description = "每日签到奖励";
        if (bonusPoints > 0) {
            description += "(连续" + (consecutiveDays + 1) + "天+" + bonusPoints + ")";
        }
        
        addPoints(userId, totalPoints, PointsRecord.TYPE_SIGN_IN, description, null);
        
        SignInResult result = new SignInResult();
        result.setPoints(totalPoints);
        result.setTodayCount(getTodaySignInCount(userId));
        result.setConsecutiveDays(consecutiveDays + 1);
        result.setDailyLimit(dailyLimit);
        result.setRemaining(Math.max(0, dailyLimit == 0 ? Integer.MAX_VALUE : dailyLimit - getTodaySignInCount(userId)));
        return result;
    }

    @Override
    public boolean todaySigned(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return this.count(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .eq(PointsRecord::getType, PointsRecord.TYPE_SIGN_IN)
                .ge(PointsRecord::getCreateTime, todayStart)) > 0;
    }

    @Override
    public int getTodaySignInCount(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return Math.toIntExact(this.count(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .eq(PointsRecord::getType, PointsRecord.TYPE_SIGN_IN)
                .ge(PointsRecord::getCreateTime, todayStart)));
    }

    @Override
    public int getConsecutiveSignInDays(Long userId) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        int consecutiveDays = 0;
        LocalDateTime checkDate = today.minusDays(1);
        
        while (true) {
            LocalDateTime dayStart = checkDate.toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            boolean hasRecord = this.count(new LambdaQueryWrapper<PointsRecord>()
                    .eq(PointsRecord::getUserId, userId)
                    .eq(PointsRecord::getType, PointsRecord.TYPE_SIGN_IN)
                    .ge(PointsRecord::getCreateTime, dayStart)
                    .lt(PointsRecord::getCreateTime, dayEnd)) > 0;
            
            if (hasRecord) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
            
            if (consecutiveDays > 365) break;
        }
        
        return consecutiveDays;
    }
    
    private PointsRule getSignInRule() {
        return this.pointsRuleService.getOne(new LambdaQueryWrapper<PointsRule>()
                .eq(PointsRule::getRuleType, PointsRule.TYPE_SIGN_IN)
                .eq(PointsRule::getIsActive, true)
                .last("LIMIT 1"));
    }
    
    private int calculateSignInPoints(PointsRule rule) {
        if (rule == null) {
            log.warn("未找到签到规则配置，使用默认值10积分");
            return 10;
        }
        
        int basePoints = rule.getPointsValue() != null ? rule.getPointsValue() : 10;
        BigDecimal ratio = rule.getPointsRatio();
        
        if (ratio != null && ratio.compareTo(BigDecimal.ZERO) > 0) {
            int calculatedPoints = new BigDecimal(basePoints).multiply(ratio).intValue();
            log.debug("签到积分计算: basePoints={}, ratio={}, calculatedPoints={}", 
                    basePoints, ratio, calculatedPoints);
            return calculatedPoints;
        }
        
        return basePoints;
    }
    
    private int calculateConsecutiveBonus(int consecutiveDays, PointsRule rule) {
        if (consecutiveDays < 1 || rule == null) return 0;
        int bonus = 0;
        if (consecutiveDays >= 7 && consecutiveDays % 7 == 0) {
            bonus = rule.getPointsValue() != null ? rule.getPointsValue() / 2 : 5;
        } else if (consecutiveDays >= 30) {
            bonus = rule.getPointsValue() != null ? rule.getPointsValue() : 10;
        }
        return bonus;
    }

    @lombok.Data
    public static class SignInResult {
        private Integer points;
        private Integer todayCount;
        private Integer consecutiveDays;
        private Integer dailyLimit;
        private Integer remaining;
    }
}
