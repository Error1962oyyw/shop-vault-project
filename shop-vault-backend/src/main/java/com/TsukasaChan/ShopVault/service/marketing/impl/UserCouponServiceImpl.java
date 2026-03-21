package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.common.CouponUtils;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.entity.marketing.CouponTemplate;
import com.TsukasaChan.ShopVault.entity.marketing.UserCoupon;
import com.TsukasaChan.ShopVault.service.marketing.ActivityService;
import com.TsukasaChan.ShopVault.service.marketing.CouponTemplateService;
import com.TsukasaChan.ShopVault.service.marketing.UserCouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.TsukasaChan.ShopVault.mapper.marketing.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    private final ActivityService activityService;
    private final CouponTemplateService couponTemplateService;

    @Override
    public void claimCoupon(Long userId, Long activityId) {
        Activity activity = activityService.getById(activityId);
        if (activity == null || activity.getType() != 3 || activity.getStatus() != 1) {
            throw new RuntimeException("该优惠券不存在或已下架");
        }

        long count = this.count(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getActivityId, activityId));
        if (count > 0) {
            throw new RuntimeException("您已经领取过这张优惠券啦！");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setActivityId(activityId);
        userCoupon.setStatus(0);
        userCoupon.setExpireTime(activity.getEndTime());
        this.save(userCoupon);
    }

    @Override
    public List<UserCoupon> listUserAvailableCoupons(Long userId) {
        List<UserCoupon> coupons = list(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0)
                .gt(UserCoupon::getExpireTime, LocalDateTime.now())
                .orderByDesc(UserCoupon::getGetTime));
        
        loadCouponTemplates(coupons);
        return coupons;
    }

    @Override
    public List<UserCoupon> listApplicableCoupons(Long userId, BigDecimal orderAmount, Long categoryId, Long productId) {
        List<UserCoupon> availableCoupons = listUserAvailableCoupons(userId);
        
        return availableCoupons.stream()
                .filter(coupon -> CouponUtils.isApplicable(coupon.getCouponTemplate(), orderAmount, categoryId, productId))
                .peek(coupon -> {
                    BigDecimal discount = calculateDiscount(coupon, orderAmount);
                    coupon.setDiscountAmount(discount);
                })
                .sorted(Comparator.comparing(UserCoupon::getDiscountAmount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public UserCoupon getBestCoupon(Long userId, BigDecimal orderAmount, Long categoryId, Long productId) {
        List<UserCoupon> applicableCoupons = listApplicableCoupons(userId, orderAmount, categoryId, productId);
        return applicableCoupons.isEmpty() ? null : applicableCoupons.get(0);
    }

    @Override
    public BigDecimal calculateDiscount(UserCoupon userCoupon, BigDecimal orderAmount) {
        if (userCoupon == null || userCoupon.getCouponTemplate() == null) {
            return BigDecimal.ZERO;
        }
        
        CouponTemplate template = userCoupon.getCouponTemplate();
        
        if (orderAmount.compareTo(template.getMinAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        
        return switch (template.getType()) {
            case 1, 3 -> template.getValue();
            case 2 -> {
                BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(template.getDiscount()));
                if (template.getMaxDiscount() != null && discount.compareTo(template.getMaxDiscount()) > 0) {
                    yield template.getMaxDiscount();
                }
                yield discount.setScale(2, RoundingMode.HALF_UP);
            }
            default -> BigDecimal.ZERO;
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCoupon(Long userCouponId, Long orderId, BigDecimal orderAmount) {
        UserCoupon userCoupon = getById(userCouponId);
        if (userCoupon == null || userCoupon.getStatus() != 0) {
            return false;
        }
        
        if (userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        BigDecimal discount = calculateDiscount(userCoupon, orderAmount);
        
        userCoupon.setStatus(1);
        userCoupon.setOrderId(orderId);
        userCoupon.setOrderAmount(orderAmount);
        userCoupon.setDiscountAmount(discount);
        userCoupon.setUseTime(LocalDateTime.now());
        
        return updateById(userCoupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireCoupons() {
        List<UserCoupon> expiredCoupons = list(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getStatus, 0)
                .lt(UserCoupon::getExpireTime, LocalDateTime.now()));
        
        for (UserCoupon coupon : expiredCoupons) {
            coupon.setStatus(2);
            updateById(coupon);
        }
        
        log.info("已处理{}张过期优惠券", expiredCoupons.size());
    }

    private void loadCouponTemplates(List<UserCoupon> coupons) {
        if (coupons.isEmpty()) {
            return;
        }
        
        List<Long> templateIds = coupons.stream()
                .map(UserCoupon::getCouponTemplateId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (!templateIds.isEmpty()) {
            List<CouponTemplate> templates = couponTemplateService.listByIds(templateIds);
            Map<Long, CouponTemplate> templateMap = templates.stream()
                    .collect(Collectors.toMap(CouponTemplate::getId, t -> t));
            
            coupons.forEach(coupon -> {
                if (coupon.getCouponTemplateId() != null) {
                    coupon.setCouponTemplate(templateMap.get(coupon.getCouponTemplateId()));
                }
            });
        }
    }
}
