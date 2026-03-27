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
    public Integer signIn(Long userId) {
        if (todaySigned(userId)) {
            throw new RuntimeException("今日已签到，请明天再来！");
        }
        
        PointsRule signInRule = getSignInRule();
        int signInPoints = calculateSignInPoints(signInRule);
        
        log.info("用户签到 - userId: {}, 签到积分: {}, 规则配置: pointsValue={}, pointsRatio={}", 
                userId, signInPoints, 
                signInRule != null ? signInRule.getPointsValue() : "null",
                signInRule != null ? signInRule.getPointsRatio() : "null");
        
        addPoints(userId, signInPoints, PointsRecord.TYPE_SIGN_IN, "每日签到奖励", null);
        return signInPoints;
    }

    @Override
    public boolean todaySigned(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return this.count(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .eq(PointsRecord::getType, PointsRecord.TYPE_SIGN_IN)
                .ge(PointsRecord::getCreateTime, todayStart)) > 0;
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
}
