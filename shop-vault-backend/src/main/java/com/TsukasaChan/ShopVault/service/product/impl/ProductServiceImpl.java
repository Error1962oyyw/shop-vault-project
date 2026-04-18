package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.dto.YoloSearchResultDto;
import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.product.ProductMapper;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.system.UserBehaviorService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.service.system.YoloMappingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final UserBehaviorService userBehaviorService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final YoloMappingService yoloMappingService;

    public ProductServiceImpl(
            UserBehaviorService userBehaviorService,
            UserService userService,
            CategoryService categoryService,
            YoloMappingService yoloMappingService) {
        this.userBehaviorService = userBehaviorService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.yoloMappingService = yoloMappingService;
    }

    @Override
    public Page<Product> getProductPage(Integer current, Integer size, String keyword, Long categoryId, String sortBy, String sortOrder) {
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);

        if (StringUtils.hasText(keyword)) wrapper.like(Product::getName, keyword);
        if (categoryId != null) {
            List<Long> childCategoryIds = categoryService.list(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId)
            ).stream().map(Category::getId).toList();
            if (childCategoryIds.isEmpty()) {
                wrapper.eq(Product::getCategoryId, categoryId);
            } else {
                List<Long> allIds = new ArrayList<>(childCategoryIds);
                allIds.add(categoryId);
                wrapper.in(Product::getCategoryId, allIds);
            }
        }

        List<String> allowedSortBy = Arrays.asList("sales", "price", "createTime", "default");
        if (!allowedSortBy.contains(sortBy)) {
            sortBy = "default";
        }
        String sortOrderParam = sortOrder != null ? sortOrder.toLowerCase() : "desc";
        if (!Arrays.asList("asc", "desc").contains(sortOrderParam)) {
            sortOrderParam = "desc";
        }

        boolean isAsc = "asc".equals(sortOrderParam);

        switch (sortBy) {
            case "sales":
                wrapper.orderByDesc(Product::getSales);
                break;
            case "price":
                if (isAsc) wrapper.orderByAsc(Product::getPrice);
                else wrapper.orderByDesc(Product::getPrice);
                break;
            case "createTime":
                if (isAsc) wrapper.orderByAsc(Product::getCreateTime);
                else wrapper.orderByDesc(Product::getCreateTime);
                break;
            case "default":
            default:
                wrapper.orderByDesc(Product::getCreateTime);
                break;
        }

        return this.page(page, wrapper);
    }

    @Override
    public Page<Product> getProductPageWithCategory(Integer current, Integer size, String keyword, Long categoryId, String sortBy, String sortOrder) {
        Page<Product> page = getProductPage(current, size, keyword, categoryId, sortBy, sortOrder);
        
        List<Long> categoryIds = page.getRecords().stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        
        if (!categoryIds.isEmpty()) {
            Map<Long, Category> categoryMap = categoryService.listByIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
            
            page.getRecords().forEach(product -> {
                Category category = categoryMap.get(product.getCategoryId());
                if (category != null) {
                    product.setCategoryName(category.getName());
                    product.setCocoId(category.getCocoId());
                    product.setYoloLabel(category.getYoloLabel());
                    if (category.getParentId() != null && category.getParentId() > 0) {
                        product.setParentCategoryId(category.getParentId());
                    }
                }
            });
        }
        
        return page;
    }

    @Override
    public Product getProductDetailWithBehavior(Long id, String username) {
        Product product = this.getById(id);
        if (product == null || product.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }

        if (username != null && !username.equals("anonymousUser")) {
            User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            if (user != null) {
                userBehaviorService.recordBehavior(user.getId(), id, 1);
            }
        }
        return product;
    }

    @Override
    public Product getProductDetailWithCategory(Long id, String username) {
        Product product = getProductDetailWithBehavior(id, username);
        
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
                product.setCocoId(category.getCocoId());
                product.setYoloLabel(category.getYoloLabel());
                if (category.getParentId() != null && category.getParentId() > 0) {
                    product.setParentCategoryId(category.getParentId());
                }
            }
        }
        
        return product;
    }

    @Override
    public void saveProductWithCategory(Product product) {
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }
        this.save(product);
    }

    @Override
    public List<YoloSearchResultDto.CategoryInfo> getHotCategories() {
        List<Category> allCategories = categoryService.list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort));

        List<Map<String, Object>> productCounts = this.getBaseMapper().countProductsByCategory();
        Map<Long, Integer> countMap = productCounts.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> ((Number) m.get("category_id")).longValue(),
                        m -> ((Number) m.get("count")).intValue()
                ));

        List<YoloSearchResultDto.CategoryInfo> hotCategories = new ArrayList<>();
        for (Category category : allCategories) {
            int productCount = countMap.getOrDefault(category.getId(), 0);
            hotCategories.add(new YoloSearchResultDto.CategoryInfo(
                    category.getId(),
                    category.getName(),
                    category.getIcon(),
                    productCount
            ));
            if (hotCategories.size() >= 10) break;
        }
        return hotCategories;
    }

    @Override
    public YoloSearchResultDto yoloSearch(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return YoloSearchResultDto.noDetection(getHotCategories());
        }

        List<Long> categoryIds = yoloMappingService.findCategoryIdsByLabels(labels);
        
        if (categoryIds.isEmpty()) {
            return YoloSearchResultDto.noMatch(labels, getHotCategories());
        }

        List<Category> categories = categoryService.listByIds(categoryIds);
        
        List<Map<String, Object>> productCounts = this.getBaseMapper().countProductsByCategory();
        Map<Long, Integer> countMap = productCounts.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> ((Number) m.get("category_id")).longValue(),
                        m -> ((Number) m.get("count")).intValue()
                ));
        
        List<YoloSearchResultDto.CategoryInfo> categoryInfos = categories.stream()
                .map(c -> new YoloSearchResultDto.CategoryInfo(
                        c.getId(), 
                        c.getName(), 
                        c.getIcon(),
                        countMap.getOrDefault(c.getId(), 0)
                ))
                .collect(Collectors.toList());

        return YoloSearchResultDto.success(labels, categoryInfos);
    }
}
