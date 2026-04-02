package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.UserCoupon;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface UserCouponService extends IService<UserCoupon> {

    void claimCoupon(Long userId, Long activityId);

    List<UserCoupon> listUserAvailableCoupons(Long userId);

    List<UserCoupon> listApplicableCoupons(Long userId, BigDecimal orderAmount, Long categoryId, Long productId);

    UserCoupon getBestCoupon(Long userId, BigDecimal orderAmount, Long categoryId, Long productId);

    BigDecimal calculateDiscount(UserCoupon userCoupon, BigDecimal orderAmount);

    boolean useCoupon(Long userCouponId, Long orderId, BigDecimal orderAmount);

    void expireCoupons();

    void loadCouponTemplates(List<UserCoupon> coupons);
}
