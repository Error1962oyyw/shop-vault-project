package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.UserPreferenceDto;
import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.manager.RecommendationEngine;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
import com.TsukasaChan.ShopVault.service.system.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController extends BaseController {

    private final RecommendationEngine recommendationService;
    private final UserPreferenceService userPreferenceService;
    private final CategoryService categoryService;

    @GetMapping("/guess-you-like")
    public Result<List<Product>> guessYouLike(@RequestParam(defaultValue = "10") int count) {
        Long userId = getOptionalUserId();
        List<Product> products = recommendationService.getRecommendationsForUser(userId, count);
        return Result.success(products);
    }

    @GetMapping("/with-explanation")
    public Result<List<Map<String, Object>>> guessYouLikeWithExplanation(@RequestParam(defaultValue = "10") int count) {
        Long userId = getOptionalUserId();
        List<Product> products = recommendationService.getRecommendationsForUser(userId, count);
        
        List<Map<String, Object>> result = products.stream().map(product -> {
            Map<String, Object> item = new HashMap<>();
            item.put("product", product);
            if (userId != null) {
                item.put("explanation", recommendationService.getRecommendationExplanation(userId, product.getId()));
            } else {
                item.put("explanation", "热门商品推荐");
            }
            return item;
        }).toList();
        
        return Result.success(result);
    }

    @GetMapping("/check-preference")
    public Result<Map<String, Object>> checkUserPreference() {
        Long userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("hasPreference", userPreferenceService.hasPreference(userId));
        result.put("preferredCategoryIds", userPreferenceService.getUserPreferenceCategoryIds(userId));
        return Result.success(result);
    }

    @GetMapping("/categories")
    public Result<List<Category>> getCategoriesForPreference() {
        return Result.success(categoryService.list());
    }

    @PostMapping("/set-preference")
    public Result<String> setUserPreference(@RequestBody UserPreferenceDto dto) {
        Long userId = getCurrentUserId();
        userPreferenceService.saveUserPreferences(userId, dto.getPreferredCategoryIds());
        return Result.success("偏好设置成功");
    }

    @GetMapping("/new-user-guide")
    public Result<Map<String, Object>> getNewUserGuide() {
        Long userId = getOptionalUserId();
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("isNewUser", true);
            result.put("needPreference", true);
        } else {
            result.put("isNewUser", !userPreferenceService.hasPreference(userId));
            result.put("needPreference", !userPreferenceService.hasPreference(userId));
        }
        
        result.put("categories", categoryService.list());
        return Result.success(result);
    }
}
