package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface UserVipInfoService extends IService<UserVipInfo> {

    UserVipInfo getByUserId(Long userId);

    void activateVip(Long userId, int days);

    BigDecimal getDiscountRate(Long userId);

    boolean isVip(Long userId);

    void extendVip(Long userId, int days);

    BigDecimal getPointsMultiplier(Long userId);
}
