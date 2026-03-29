package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface UserVipInfoService extends IService<UserVipInfo> {
    UserVipInfo getByUserId(Long userId);
    
    void activateVip(Long userId, int days);
    
    void activateSvip(Long userId, int days);
    
    void activateVipWithLevel(Long userId, int days, int vipLevel);
    
    BigDecimal getDiscountRate(Long userId);
    
    boolean isVip(Long userId);
    
    boolean isSvip(Long userId);
    
    void extendVip(Long userId, int days);
    
    void extendVipWithLevel(Long userId, int days, int vipLevel);
    
    BigDecimal getPointsMultiplier(Long userId);
    
    void updateVipLevel(Long userId, Integer level, Integer days);
}
