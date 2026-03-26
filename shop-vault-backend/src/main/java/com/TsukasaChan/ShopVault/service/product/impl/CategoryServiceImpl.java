package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.mapper.product.CategoryMapper;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    @Cacheable(value = "categoryCache", key = "'categoryTree'")
    public List<Category> listCategories() {
        List<Category> allCategories = this.list();

        return allCategories.stream()
                .filter(category -> category.getParentId() == 0)
                .peek(category -> category.setChildren(getChildren(category, allCategories)))
                .sorted((c1, c2) -> {
                    int sort1 = c1.getSort() == null ? 0 : c1.getSort();
                    int sort2 = c2.getSort() == null ? 0 : c2.getSort();
                    return Integer.compare(sort1, sort2);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> listCategoriesWithYolo() {
        List<Category> allCategories = this.list();
        
        return allCategories.stream()
                .filter(category -> category.getLevel() == 2)
                .sorted((c1, c2) -> {
                    int sort1 = c1.getSort() == null ? 0 : c1.getSort();
                    int sort2 = c2.getSort() == null ? 0 : c2.getSort();
                    return Integer.compare(sort1, sort2);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Category getCategoryById(Long id) {
        return this.getById(id);
    }

    @Override
    public Category getCategoryByYoloLabel(String yoloLabel) {
        return this.getOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getYoloLabel, yoloLabel)
                .last("LIMIT 1"));
    }
    
    private List<Category> getChildren(Category root, List<Category> allCategories) {
        return allCategories.stream()
                .filter(category -> category.getParentId().equals(root.getId()))
                .peek(category -> category.setChildren(getChildren(category, allCategories)))
                .sorted((c1, c2) -> {
                    int sort1 = c1.getSort() == null ? 0 : c1.getSort();
                    int sort2 = c2.getSort() == null ? 0 : c2.getSort();
                    return Integer.compare(sort1, sort2);
                })
                .collect(Collectors.toList());
    }
}
