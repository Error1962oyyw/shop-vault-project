package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.config.PointsConfig;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.marketing.PointsRecordMapper;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements PointsRecordService {

    private final UserService userService;
    private final PointsConfig pointsConfig;

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
        
        int signInPoints = pointsConfig.getPointsToCashRate() / 10;
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
}
