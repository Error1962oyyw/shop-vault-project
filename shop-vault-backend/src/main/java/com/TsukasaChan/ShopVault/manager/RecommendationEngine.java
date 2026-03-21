package com.TsukasaChan.ShopVault.manager;

import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.system.UserPreferenceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationEngine {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserPreferenceService userPreferenceService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RECOMMENDATION_CACHE_KEY = "recommendation:user:";
    private static final String BEHAVIOR_CACHE_KEY = "behavior:user:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    public List<Product> getRecommendationsForUser(Long userId, int recommendCount) {
        String cacheKey = RECOMMENDATION_CACHE_KEY + userId;
        
        @SuppressWarnings("unchecked")
        List<Long> cachedProductIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProductIds != null && !cachedProductIds.isEmpty()) {
            List<Product> cachedProducts = productService.listByIds(cachedProductIds);
            if (cachedProducts.size() >= recommendCount) {
                return cachedProducts.subList(0, recommendCount);
            }
        }

        List<Order> userOrders = orderService.list(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId));
        
        if (userOrders.isEmpty()) {
            return getNewUserRecommendations(userId, recommendCount);
        }

        List<Long> userOrderIds = userOrders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> userOrderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, userOrderIds));
        Set<Long> userPurchasedProductIds = userOrderItems.stream().map(OrderItem::getProductId).collect(Collectors.toSet());

        if (userPurchasedProductIds.isEmpty()) {
            return getNewUserRecommendations(userId, recommendCount);
        }

        List<Product> recommendations = calculateItemBasedCF(userPurchasedProductIds, recommendCount);
        
        if (recommendations.isEmpty()) {
            recommendations = getFallbackRecommendations(recommendCount);
        }

        cacheRecommendations(userId, recommendations.stream().map(Product::getId).collect(Collectors.toList()));
        
        return recommendations;
    }

    private List<Product> calculateItemBasedCF(Set<Long> userPurchasedProductIds, int recommendCount) {
        Map<Long, Map<Long, Integer>> itemCoOccurrenceMatrix = buildItemCoOccurrenceMatrix();

        Map<Long, Integer> recommendationScores = new HashMap<>();
        for (Long purchasedId : userPurchasedProductIds) {
            Map<Long, Integer> relatedProducts = itemCoOccurrenceMatrix.getOrDefault(purchasedId, new HashMap<>());
            for (Map.Entry<Long, Integer> entry : relatedProducts.entrySet()) {
                Long relatedProductId = entry.getKey();
                if (!userPurchasedProductIds.contains(relatedProductId)) {
                    recommendationScores.put(relatedProductId,
                            recommendationScores.getOrDefault(relatedProductId, 0) + entry.getValue());
                }
            }
        }

        List<Long> recommendedProductIds = recommendationScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(recommendCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (recommendedProductIds.isEmpty()) {
            return List.of();
        }

        return productService.listByIds(recommendedProductIds);
    }

    public List<Product> getNewUserRecommendations(Long userId, int recommendCount) {
        List<Long> preferredCategoryIds = userPreferenceService.getUserPreferenceCategoryIds(userId);
        
        if (!preferredCategoryIds.isEmpty()) {
            List<Product> preferenceBased = productService.list(new LambdaQueryWrapper<Product>()
                    .in(Product::getCategoryId, preferredCategoryIds)
                    .eq(Product::getStatus, 1)
                    .orderByDesc(Product::getSales)
                    .last("LIMIT " + recommendCount));
            
            if (!preferenceBased.isEmpty()) {
                return preferenceBased;
            }
        }

        return getFallbackRecommendations(recommendCount);
    }

    private List<Product> getFallbackRecommendations(int limit) {
        return productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit));
    }

    public void recordBehaviorAndUpdateRecommendation(Long userId, Long productId, int behaviorType) {
        String behaviorKey = BEHAVIOR_CACHE_KEY + userId;
        
        @SuppressWarnings("unchecked")
        Map<Long, Integer> recentBehaviors = (Map<Long, Integer>) redisTemplate.opsForValue().get(behaviorKey);
        if (recentBehaviors == null) {
            recentBehaviors = new HashMap<>();
        }
        
        int weight = getBehaviorWeight(behaviorType);
        recentBehaviors.put(productId, recentBehaviors.getOrDefault(productId, 0) + weight);
        
        redisTemplate.opsForValue().set(behaviorKey, recentBehaviors, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        invalidateRecommendationCache(userId);
    }

    private int getBehaviorWeight(int behaviorType) {
        return switch (behaviorType) {
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 5;
            default -> 1;
        };
    }

    private void cacheRecommendations(Long userId, List<Long> productIds) {
        String cacheKey = RECOMMENDATION_CACHE_KEY + userId;
        redisTemplate.opsForValue().set(cacheKey, productIds, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    private void invalidateRecommendationCache(Long userId) {
        String cacheKey = RECOMMENDATION_CACHE_KEY + userId;
        redisTemplate.delete(cacheKey);
    }

    public String getRecommendationExplanation(Long userId, Long productId) {
        List<Order> userOrders = orderService.list(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId));
        if (userOrders.isEmpty()) {
            return "热门商品推荐";
        }

        Set<Long> userPurchasedProductIds = getUserPurchasedProductIds(userOrders);
        Map<Long, Map<Long, Integer>> itemCoOccurrenceMatrix = buildItemCoOccurrenceMatrix();

        for (Long purchasedId : userPurchasedProductIds) {
            Map<Long, Integer> relatedProducts = itemCoOccurrenceMatrix.getOrDefault(purchasedId, new HashMap<>());
            if (relatedProducts.containsKey(productId)) {
                Product purchasedProduct = productService.getById(purchasedId);
                if (purchasedProduct != null) {
                    return "因您购买过「" + purchasedProduct.getName() + "」，为您推荐";
                }
            }
        }

        return "猜你喜欢";
    }

    private Set<Long> getUserPurchasedProductIds(List<Order> userOrders) {
        List<Long> userOrderIds = userOrders.stream().map(Order::getId).toList();
        List<OrderItem> userOrderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, userOrderIds));
        return userOrderItems.stream().map(OrderItem::getProductId).collect(Collectors.toSet());
    }

    private Map<Long, Map<Long, Integer>> buildItemCoOccurrenceMatrix() {
        List<OrderItem> allOrderItems = orderItemService.list();
        Map<Long, List<Long>> orderToProductsMap = allOrderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId,
                        Collectors.mapping(OrderItem::getProductId, Collectors.toList())));

        Map<Long, Map<Long, Integer>> itemCoOccurrenceMatrix = new HashMap<>();
        for (List<Long> productsInOrder : orderToProductsMap.values()) {
            for (Long p1 : productsInOrder) {
                itemCoOccurrenceMatrix.putIfAbsent(p1, new HashMap<>());
                for (Long p2 : productsInOrder) {
                    if (!p1.equals(p2)) {
                        Map<Long, Integer> coMap = itemCoOccurrenceMatrix.get(p1);
                        coMap.put(p2, coMap.getOrDefault(p2, 0) + 1);
                    }
                }
            }
        }
        return itemCoOccurrenceMatrix;
    }
}
