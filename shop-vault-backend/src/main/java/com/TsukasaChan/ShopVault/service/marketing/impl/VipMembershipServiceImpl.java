package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.entity.marketing.BalanceRecord;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.marketing.VipMembership;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.marketing.VipMembershipMapper;
import com.TsukasaChan.ShopVault.service.marketing.BalanceRecordService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VipMembershipServiceImpl extends ServiceImpl<VipMembershipMapper, VipMembership> implements VipMembershipService {

    private final UserService userService;
    private final UserVipInfoService userVipInfoService;
    private final PointsRecordService pointsRecordService;
    private final BalanceRecordService balanceRecordService;

    private static final int VIP_MONTHLY_POINTS = 1000;
    private static final int VIP_YEARLY_POINTS = 10000;
    private static final int SVIP_YEARLY_POINTS = 20000;
    
    private static final BigDecimal VIP_MONTHLY_PRICE = new BigDecimal("99.00");
    private static final BigDecimal VIP_YEARLY_PRICE = new BigDecimal("999.00");
    private static final BigDecimal SVIP_YEARLY_PRICE = new BigDecimal("1499.00");

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
    @Transactional(rollbackFor = Exception.class)
    public void purchaseVip(Long userId, int vipType, String paymentMethod) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int days;
        int vipLevel;
        int pointsCost = 0;
        BigDecimal balanceCost = BigDecimal.ZERO;
        BigDecimal balanceBefore = BigDecimal.ZERO;
        BigDecimal balanceAfter = BigDecimal.ZERO;
        String description;

        if (vipType == VipMembership.TYPE_MONTHLY) {
            days = 30;
            vipLevel = VipMembership.LEVEL_VIP;
            description = "购买VIP月卡";
            if ("points".equals(paymentMethod)) {
                pointsCost = VIP_MONTHLY_POINTS;
            } else {
                balanceCost = VIP_MONTHLY_PRICE;
            }
        } else if (vipType == VipMembership.TYPE_YEARLY) {
            days = 365;
            vipLevel = VipMembership.LEVEL_VIP;
            description = "购买VIP年卡";
            if ("points".equals(paymentMethod)) {
                pointsCost = VIP_YEARLY_POINTS;
            } else {
                balanceCost = VIP_YEARLY_PRICE;
            }
        } else if (vipType == VipMembership.TYPE_SVIP_YEARLY) {
            days = 365;
            vipLevel = VipMembership.LEVEL_SVIP;
            description = "购买SVIP年卡";
            if ("points".equals(paymentMethod)) {
                pointsCost = SVIP_YEARLY_POINTS;
            } else {
                balanceCost = SVIP_YEARLY_PRICE;
            }
        } else {
            throw new RuntimeException("无效的VIP类型");
        }

        if ("points".equals(paymentMethod)) {
            if (user.getPoints() < pointsCost) {
                throw new RuntimeException("积分不足，需要 " + pointsCost + " 积分");
            }
            user.setPoints(user.getPoints() - pointsCost);
            
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setPoints(-pointsCost);
            record.setType(PointsRecord.TYPE_EXCHANGE);
            record.setDescription(description);
            pointsRecordService.save(record);
        } else if ("balance".equals(paymentMethod)) {
            BigDecimal userBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
            if (userBalance.compareTo(balanceCost) < 0) {
                throw new RuntimeException("余额不足，需要 ¥" + balanceCost);
            }
            balanceBefore = userBalance;
            balanceAfter = userBalance.subtract(balanceCost);
            user.setBalance(balanceAfter);
        } else {
            throw new RuntimeException("无效的支付方式");
        }

        userService.updateById(user);

        VipMembership membership = new VipMembership();
        membership.setUserId(userId);
        membership.setType(vipType);
        membership.setVipLevel(vipLevel);
        membership.setPointsCost(pointsCost);
        membership.setSource("points".equals(paymentMethod) ? "POINTS_EXCHANGE" : "BALANCE_PURCHASE");
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
        
        if (!this.save(membership)) {
            throw new RuntimeException("VIP会员记录保存失败");
        }

        Long membershipId = membership.getId();
        if (membershipId == null) {
            throw new RuntimeException("VIP会员记录ID获取失败，数据异常");
        }

        if ("balance".equals(paymentMethod)) {
            balanceRecordService.recordBalanceChange(
                    userId,
                    balanceCost.negate(),
                    balanceBefore,
                    balanceAfter,
                    BalanceRecord.TYPE_VIP_PURCHASE,
                    description,
                    membershipId
            );
        }

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
