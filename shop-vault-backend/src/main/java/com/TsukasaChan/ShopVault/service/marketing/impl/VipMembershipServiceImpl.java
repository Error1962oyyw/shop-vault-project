package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.marketing.VipMembership;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.marketing.VipMembershipMapper;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.service.marketing.VipMembershipService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VipMembershipServiceImpl extends ServiceImpl<VipMembershipMapper, VipMembership> implements VipMembershipService {

    private final UserService userService;
    private final UserVipInfoService userVipInfoService;
    private final PointsRecordService pointsRecordService;

    private static final int VIP_MONTHLY_POINTS = 1000;
    private static final int VIP_YEARLY_POINTS = 10000;
    private static final int SVIP_YEARLY_POINTS = 15000;

    @Override
    public VipMembership getActiveVipByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<VipMembership>()
                .eq(VipMembership::getUserId, userId)
                .eq(VipMembership::getStatus, VipMembership.STATUS_ACTIVE)
                .gt(VipMembership::getEndTime, LocalDateTime.now()));
    }

    @Override
    public boolean isVip(Long userId) {
        return getActiveVipByUserId(userId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeVip(Long userId, int vipType) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int pointsCost;
        int days;
        int vipLevel;
        String description;
        
        if (vipType == VipMembership.TYPE_MONTHLY) {
            pointsCost = VIP_MONTHLY_POINTS;
            days = 30;
            vipLevel = VipMembership.LEVEL_VIP;
            description = "兑换VIP月卡";
        } else if (vipType == VipMembership.TYPE_YEARLY) {
            pointsCost = VIP_YEARLY_POINTS;
            days = 365;
            vipLevel = VipMembership.LEVEL_VIP;
            description = "兑换VIP年卡";
        } else if (vipType == VipMembership.TYPE_SVIP_YEARLY) {
            pointsCost = SVIP_YEARLY_POINTS;
            days = 365;
            vipLevel = VipMembership.LEVEL_SVIP;
            description = "兑换SVIP年卡";
        } else {
            throw new RuntimeException("无效的VIP类型");
        }

        if (user.getPoints() < pointsCost) {
            throw new RuntimeException("积分不足，兑换需要 " + pointsCost + " 积分");
        }

        user.setPoints(user.getPoints() - pointsCost);
        userService.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(-pointsCost);
        record.setType(PointsRecord.TYPE_EXCHANGE);
        record.setDescription(description);
        pointsRecordService.save(record);

        VipMembership membership = new VipMembership();
        membership.setUserId(userId);
        membership.setType(vipType);
        membership.setVipLevel(vipLevel);
        membership.setPointsCost(pointsCost);
        membership.setSource("POINTS_EXCHANGE");
        membership.setStatus(VipMembership.STATUS_ACTIVE);
        membership.setCreateTime(LocalDateTime.now());

        VipMembership existingVip = getActiveVipByUserId(userId);
        if (existingVip != null) {
            membership.setStartTime(existingVip.getEndTime());
            membership.setEndTime(existingVip.getEndTime().plusDays(days));
        } else {
            membership.setStartTime(LocalDateTime.now());
            membership.setEndTime(LocalDateTime.now().plusDays(days));
        }
        this.save(membership);

        userVipInfoService.extendVipWithLevel(userId, days, vipLevel);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkAndExpireVip() {
        List<VipMembership> expiredList = this.list(new LambdaQueryWrapper<VipMembership>()
                .eq(VipMembership::getStatus, VipMembership.STATUS_ACTIVE)
                .lt(VipMembership::getEndTime, LocalDateTime.now()));

        for (VipMembership membership : expiredList) {
            membership.setStatus(VipMembership.STATUS_EXPIRED);
            this.updateById(membership);

            VipMembership activeVip = getActiveVipByUserId(membership.getUserId());
            if (activeVip == null) {
                userVipInfoService.getByUserId(membership.getUserId());
            }
        }
    }

    @Override
    public List<VipMembership> getVipHistory(Long userId) {
        return this.list(new LambdaQueryWrapper<VipMembership>()
                .eq(VipMembership::getUserId, userId)
                .orderByDesc(VipMembership::getCreateTime));
    }
}
