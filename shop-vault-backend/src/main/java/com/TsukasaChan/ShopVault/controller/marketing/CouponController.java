package com.TsukasaChan.ShopVault.controller.marketing;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.SecurityUtils;
import com.TsukasaChan.ShopVault.dto.CouponTemplateDto;
import com.TsukasaChan.ShopVault.entity.marketing.CouponTemplate;
import com.TsukasaChan.ShopVault.entity.marketing.UserCoupon;
import com.TsukasaChan.ShopVault.service.marketing.CouponTemplateService;
import com.TsukasaChan.ShopVault.service.marketing.UserCouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponTemplateService couponTemplateService;
    private final UserCouponService userCouponService;

    @GetMapping("/available")
    public Result<List<CouponTemplate>> listAvailable() {
        return Result.success(couponTemplateService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<CouponTemplate> getById(@PathVariable Long id) {
        return Result.success(couponTemplateService.getAvailableById(id));
    }

    @PostMapping("/{id}/receive")
    public Result<Boolean> receiveCoupon(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(couponTemplateService.receiveCoupon(id, userId));
    }

    @GetMapping("/my")
    public Result<List<UserCoupon>> listMyCoupons(@RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        wrapper.orderByDesc(UserCoupon::getGetTime);
        return Result.success(userCouponService.list(wrapper));
    }

    @GetMapping("/my/available")
    public Result<List<UserCoupon>> listMyAvailableCoupons() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userCouponService.listUserAvailableCoupons(userId));
    }

    @GetMapping("/my/applicable")
    public Result<List<UserCoupon>> listApplicableCoupons(
            @RequestParam BigDecimal orderAmount,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userCouponService.listApplicableCoupons(userId, orderAmount, categoryId, productId));
    }

    @GetMapping("/my/best")
    public Result<UserCoupon> getBestCoupon(
            @RequestParam BigDecimal orderAmount,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userCouponService.getBestCoupon(userId, orderAmount, categoryId, productId));
    }

    @PostMapping("/{id}/use")
    public Result<Boolean> useCoupon(
            @PathVariable Long id,
            @RequestParam Long orderId,
            @RequestParam BigDecimal orderAmount) {
        return Result.success(userCouponService.useCoupon(id, orderId, orderAmount));
    }
}

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
class CouponAdminController {

    private final CouponTemplateService couponTemplateService;
    private final UserCouponService userCouponService;

    @GetMapping
    public Result<Page<CouponTemplate>> list(Page<CouponTemplate> page) {
        return Result.success(couponTemplateService.page(page, 
                new LambdaQueryWrapper<CouponTemplate>().orderByDesc(CouponTemplate::getCreateTime)));
    }

    @PostMapping
    public Result<CouponTemplate> create(@RequestBody CouponTemplateDto dto) {
        CouponTemplate template = new CouponTemplate();
        template.setName(dto.getName());
        template.setType(dto.getType());
        template.setValue(dto.getValue());
        template.setDiscount(dto.getDiscount());
        template.setMinAmount(dto.getMinAmount() != null ? dto.getMinAmount() : BigDecimal.ZERO);
        template.setMaxDiscount(dto.getMaxDiscount());
        template.setTotalCount(dto.getTotalCount() != null ? dto.getTotalCount() : 0);
        template.setUsedCount(0);
        template.setPerLimit(dto.getPerLimit() != null ? dto.getPerLimit() : 1);
        template.setScopeType(dto.getScopeType() != null ? dto.getScopeType() : 1);
        template.setScopeIds(dto.getScopeIds());
        template.setValidType(dto.getValidType() != null ? dto.getValidType() : 2);
        template.setValidStartTime(dto.getValidStartTime());
        template.setValidEndTime(dto.getValidEndTime());
        template.setValidDays(dto.getValidDays() != null ? dto.getValidDays() : 7);
        template.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        couponTemplateService.save(template);
        return Result.success(template);
    }

    @PutMapping("/{id}")
    public Result<CouponTemplate> update(@PathVariable Long id, @RequestBody CouponTemplateDto dto) {
        CouponTemplate template = couponTemplateService.getById(id);
        if (template == null) {
            return Result.error("优惠券不存在");
        }
        
        if (dto.getName() != null) template.setName(dto.getName());
        if (dto.getType() != null) template.setType(dto.getType());
        if (dto.getValue() != null) template.setValue(dto.getValue());
        if (dto.getDiscount() != null) template.setDiscount(dto.getDiscount());
        if (dto.getMinAmount() != null) template.setMinAmount(dto.getMinAmount());
        if (dto.getMaxDiscount() != null) template.setMaxDiscount(dto.getMaxDiscount());
        if (dto.getTotalCount() != null) template.setTotalCount(dto.getTotalCount());
        if (dto.getPerLimit() != null) template.setPerLimit(dto.getPerLimit());
        if (dto.getScopeType() != null) template.setScopeType(dto.getScopeType());
        if (dto.getScopeIds() != null) template.setScopeIds(dto.getScopeIds());
        if (dto.getValidType() != null) template.setValidType(dto.getValidType());
        if (dto.getValidStartTime() != null) template.setValidStartTime(dto.getValidStartTime());
        if (dto.getValidEndTime() != null) template.setValidEndTime(dto.getValidEndTime());
        if (dto.getValidDays() != null) template.setValidDays(dto.getValidDays());
        if (dto.getStatus() != null) template.setStatus(dto.getStatus());
        
        couponTemplateService.updateById(template);
        return Result.success(template);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponTemplateService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        CouponTemplate template = couponTemplateService.getById(id);
        if (template != null) {
            template.setStatus(status);
            couponTemplateService.updateById(template);
        }
        return Result.success();
    }

    @PostMapping("/expire")
    public Result<Void> expireCoupons() {
        userCouponService.expireCoupons();
        return Result.success();
    }
}
