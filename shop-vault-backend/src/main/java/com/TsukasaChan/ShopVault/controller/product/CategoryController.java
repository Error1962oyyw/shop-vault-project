package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        return Result.success(categoryService.listCategories());
    }

    @GetMapping("/list-with-yolo")
    public Result<List<Category>> getCategoryListWithYolo() {
        return Result.success(categoryService.listCategoriesWithYolo());
    }

    @GetMapping("/detail/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error(404, "分类不存在");
        }
        return Result.success(category);
    }

    @GetMapping("/by-yolo-label/{yoloLabel}")
    public Result<Category> getCategoryByYoloLabel(@PathVariable String yoloLabel) {
        Category category = categoryService.getCategoryByYoloLabel(yoloLabel);
        if (category == null) {
            return Result.error(404, "未找到对应的分类");
        }
        return Result.success(category);
    }

    @GetMapping("/raw")
    public Result<List<Category>> getRawCategories() {
        List<Category> all = categoryService.list(new LambdaQueryWrapper<Category>().orderByAsc(Category::getLevel).orderByAsc(Category::getSort));
        return Result.success(all);
    }

    @PostMapping("/clear-cache")
    public Result<String> clearCache() {
        categoryService.clearCategoryCache();
        return Result.success("缓存已清除");
    }
}
