package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class StockTransactionalHelper {

    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STOCK_CACHE_KEY = "stock:product:";

    @Transactional(rollbackFor = Exception.class)
    public boolean deductStockTransactional(Long productId, Integer quantity) {
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() == 0) {
            log.warn("商品不存在或已下架: {}", productId);
            return false;
        }

        if (product.getStock() < quantity) {
            log.warn("库存不足: productId={}, 当前库存={}, 请求数量={}", productId, product.getStock(), quantity);
            return false;
        }

        int updated = productService.getBaseMapper().update(null, 
                new LambdaUpdateWrapper<Product>()
                        .set(Product::getStock, product.getStock() - quantity)
                        .eq(Product::getId, productId)
                        .ge(Product::getStock, quantity)
                        .eq(Product::getStatus, 1));

        if (updated > 0) {
            refreshStockCache(productId);
            log.info("库存扣减成功: productId={}, quantity={}", productId, quantity);
            return true;
        }

        log.warn("库存扣减失败(并发冲突): productId={}", productId);
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreStockTransactional(Long productId, Integer quantity) {
        productService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<Product>()
                        .setSql("stock = stock + {0}", quantity)
                        .eq(Product::getId, productId));

        refreshStockCache(productId);
        log.info("库存恢复成功: productId={}, quantity={}", productId, quantity);
    }

    private void refreshStockCache(Long productId) {
        String cacheKey = STOCK_CACHE_KEY + productId;
        Product product = productService.getById(productId);
        if (product != null) {
            redisTemplate.opsForValue().set(cacheKey, (Object) product.getStock(), 1, TimeUnit.HOURS);
        } else {
            redisTemplate.delete(cacheKey);
        }
    }
}
