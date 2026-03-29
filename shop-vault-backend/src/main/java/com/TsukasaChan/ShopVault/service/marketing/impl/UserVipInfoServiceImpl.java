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
    private static final BigDecimal SVIP_DISCOUNT_RATE = new BigDecimal("0.95");
    private static final BigDecimal NORMAL_DISCOUNT_RATE = BigDecimal.ONE;
    private static final BigDecimal VIP_POINTS_MULTIPLIER = new BigDecimal("1.25");
    private static final BigDecimal SVIP_POINTS_MULTIPLIER = new BigDecimal("1.5");

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
        activateVipWithLevel(userId, days, UserVipInfo.LEVEL_VIP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateSvip(Long userId, int days) {
        UserVipInfo vipInfo = getByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireTime;

        if (vipInfo.getVipExpireTime() == null || vipInfo.getVipExpireTime().isBefore(now)) {
            newExpireTime = now.plusDays(days);
        } else {
            newExpireTime = vipInfo.getVipExpireTime().plusDays(days);
        }

        vipInfo.setVipLevel(UserVipInfo.LEVEL_SVIP);
        vipInfo.setDiscountRate(SVIP_DISCOUNT_RATE);
        vipInfo.setVipExpireTime(newExpireTime);
        vipInfo.setTotalVipDays(vipInfo.getTotalVipDays() + days);
        vipInfo.setUpdateTime(now);
        this.updateById(vipInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateVipWithLevel(Long userId, int days, int vipLevel) {
        UserVipInfo vipInfo = getByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireTime;

        if (vipInfo.getVipExpireTime() == null || vipInfo.getVipExpireTime().isBefore(now)) {
            newExpireTime = now.plusDays(days);
        } else {
            newExpireTime = vipInfo.getVipExpireTime().plusDays(days);
        }

        BigDecimal discountRate = NORMAL_DISCOUNT_RATE;
        if (vipLevel == UserVipInfo.LEVEL_VIP) {
            discountRate = VIP_DISCOUNT_RATE;
        } else if (vipLevel == UserVipInfo.LEVEL_SVIP) {
            discountRate = SVIP_DISCOUNT_RATE;
        }

        vipInfo.setVipLevel(vipLevel);
        vipInfo.setDiscountRate(discountRate);
        vipInfo.setVipExpireTime(newExpireTime);
        vipInfo.setTotalVipDays(vipInfo.getTotalVipDays() + days);
        vipInfo.setUpdateTime(now);
        this.updateById(vipInfo);
    }

    @Override
    public BigDecimal getDiscountRate(Long userId) {
        UserVipInfo vipInfo = getByUserId(userId);
        if (vipInfo.getVipExpireTime() != null && 
            vipInfo.getVipExpireTime().isAfter(LocalDateTime.now())) {
            int level = vipInfo.getVipLevel();
            if (level == UserVipInfo.LEVEL_SVIP) {
                return SVIP_DISCOUNT_RATE;
            } else if (level == UserVipInfo.LEVEL_VIP) {
                return VIP_DISCOUNT_RATE;
            }
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
    public boolean isSvip(Long userId) {
        UserVipInfo vipInfo = getByUserId(userId);
        return vipInfo.getVipLevel() == UserVipInfo.LEVEL_SVIP && 
               vipInfo.getVipExpireTime() != null && 
               vipInfo.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extendVip(Long userId, int days) {
        activateVip(userId, days);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extendVipWithLevel(Long userId, int days, int vipLevel) {
        activateVipWithLevel(userId, days, vipLevel);
    }

    @Override
    public BigDecimal getPointsMultiplier(Long userId) {
        UserVipInfo vipInfo = getByUserId(userId);
        if (vipInfo.getVipExpireTime() != null && vipInfo.getVipExpireTime().isAfter(LocalDateTime.now())) {
            int level = vipInfo.getVipLevel();
            if (level == UserVipInfo.LEVEL_SVIP) {
                return SVIP_POINTS_MULTIPLIER;
            } else if (level == UserVipInfo.LEVEL_VIP) {
                return VIP_POINTS_MULTIPLIER;
            }
        }
        return BigDecimal.ONE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVipLevel(Long userId, Integer level, Integer days) {
        if (level == null) {
            throw new IllegalArgumentException("会员等级不能为空");
        }
        
        UserVipInfo vipInfo = getByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        
        vipInfo.setVipLevel(level);
        
        if (level == UserVipInfo.LEVEL_NORMAL) {
            vipInfo.setDiscountRate(NORMAL_DISCOUNT_RATE);
            vipInfo.setVipExpireTime(null);
        } else if (level == UserVipInfo.LEVEL_VIP) {
            vipInfo.setDiscountRate(VIP_DISCOUNT_RATE);
            if (days != null && days > 0) {
                vipInfo.setVipExpireTime(now.plusDays(days));
                vipInfo.setTotalVipDays(vipInfo.getTotalVipDays() + days);
            }
        } else if (level == UserVipInfo.LEVEL_SVIP) {
            vipInfo.setDiscountRate(SVIP_DISCOUNT_RATE);
            if (days != null && days > 0) {
                vipInfo.setVipExpireTime(now.plusDays(days));
                vipInfo.setTotalVipDays(vipInfo.getTotalVipDays() + days);
            }
        }
        
        vipInfo.setUpdateTime(now);
        this.updateById(vipInfo);
    }
}
