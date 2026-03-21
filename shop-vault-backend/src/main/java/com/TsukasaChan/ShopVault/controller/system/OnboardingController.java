package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.SecurityUtils;
import com.TsukasaChan.ShopVault.dto.OnboardingDto;
import com.TsukasaChan.ShopVault.dto.OnboardingStatusDto;
import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/status")
    public Result<OnboardingStatusDto> getOnboardingStatus() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        OnboardingStatusDto status = new OnboardingStatusDto();
        status.setIsFirstLogin(userService.isFirstLogin(userId));
        status.setHasCompletedOnboarding(userService.hasCompletedOnboarding(userId));
        
        return Result.success(status);
    }

    @GetMapping("/categories")
    public Result<List<Category>> getCategoriesForSelection() {
        return Result.success(categoryService.list());
    }

    @PostMapping("/complete")
    public Result<Void> completeOnboarding(@RequestBody OnboardingDto dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.completeOnboarding(userId, dto.getPreferredCategoryIds());
        return Result.success();
    }

    @PostMapping("/skip")
    public Result<Void> skipOnboarding() {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.completeFirstLogin(userId);
        return Result.success();
    }
}
