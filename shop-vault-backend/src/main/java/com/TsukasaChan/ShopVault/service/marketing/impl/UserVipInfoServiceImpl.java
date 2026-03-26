package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.TsukasaChan.ShopVault.mapper.marketing.UserVipInfoMapper;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserVipInfoServiceImpl extends ServiceImpl<UserVipInfoMapper, UserVipInfo> implements UserVipInfoService {

    private static final BigDecimal VIP_DISCOUNT_RATE = new BigDecimal("0.98");
    private static final BigDecimal NORMAL_DISCOUNT_RATE = BigDecimal.ONE;
    private static final BigDecimal VIP_POINTS_MULTIPLIER = new BigDecimal("1.5");

    @Override
    public UserVipInfo getByUserId(Long userId) {
        UserVipInfo vipInfo = this.getOne(new LambdaQueryWrapper<UserVipInfo>()
                .eq(UserVipInfo::getUserId, userId));
        if (vipInfo == null) {
            vipInfo = createDefaultVipInfo(userId);
        }
        return vipInfo;
    }

    private UserVipInfo createDefaultVipInfo(Long userId) {
        UserVipInfo vipInfo = new UserVipInfo();
        vipInfo.setUserId(userId);
        vipInfo.setVipLevel(UserVipInfo.LEVEL_NORMAL);
        vipInfo.setDiscountRate(NORMAL_DISCOUNT_RATE);
        vipInfo.setTotalVipDays(0);
        vipInfo.setCreateTime(LocalDateTime.now());
        vipInfo.setUpdateTime(LocalDateTime.now());
        this.save(vipInfo);
        return vipInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateVip(Long userId, int days) {
        UserVipInfo vipInfo = getByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireTime;

        if (vipInfo.getVipExpireTime() == null || vipInfo.getVipExpireTime().isBefore(now)) {
            newExpireTime = now.plusDays(days);
        } else {
            newExpireTime = vipInfo.getVipExpireTime().plusDays(days);
        }

        vipInfo.setVipLevel(UserVipInfo.LEVEL_VIP);
        vipInfo.setDiscountRate(VIP_DISCOUNT_RATE);
        vipInfo.setVipExpireTime(newExpireTime);
        vipInfo.setTotalVipDays(vipInfo.getTotalVipDays() + days);
        vipInfo.setUpdateTime(now);
        this.updateById(vipInfo);
    }

    @Override
    public BigDecimal getDiscountRate(Long userId) {
        UserVipInfo vipInfo = getByUserId(userId);
        if (vipInfo.getVipLevel() >= UserVipInfo.LEVEL_VIP && 
            vipInfo.getVipExpireTime() != null && 
            vipInfo.getVipExpireTime().isAfter(LocalDateTime.now())) {
            return vipInfo.getDiscountRate();
        }
        return NORMAL_DISCOUNT_RATE;
    }

    @Override
    public boolean isVip(Long userId) {
        UserVipInfo vipInfo = getByUserId(userId);
        return vipInfo.getVipLevel() >= UserVipInfo.LEVEL_VIP && 
               vipInfo.getVipExpireTime() != null && 
               vipInfo.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extendVip(Long userId, int days) {
        activateVip(userId, days);
    }

    @Override
    public BigDecimal getPointsMultiplier(Long userId) {
        if (isVip(userId)) {
            return VIP_POINTS_MULTIPLIER;
        }
        return BigDecimal.ONE;
    }
}
