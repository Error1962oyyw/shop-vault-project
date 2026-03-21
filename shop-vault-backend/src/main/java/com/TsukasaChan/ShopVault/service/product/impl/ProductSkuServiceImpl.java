package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.common.RedisDistributedLock;
import com.TsukasaChan.ShopVault.entity.product.ProductSku;
import com.TsukasaChan.ShopVault.mapper.product.ProductSkuMapper;
import com.TsukasaChan.ShopVault.service.product.ProductSkuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    private final RedisDistributedLock redisDistributedLock;

    @Override
    public List<ProductSku> listByProductId(Long productId) {
        return list(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .orderByAsc(ProductSku::getId));
    }

    @Override
    public ProductSku getBySkuCode(String skuCode) {
        return getOne(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSkuCode, skuCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long skuId, Integer quantity) {
        String lockKey = "sku_stock_lock:" + skuId;
        String lockValue = redisDistributedLock.tryLockWithWait(lockKey, 3, 10, TimeUnit.SECONDS);
        
        if (lockValue == null) {
            log.warn("[SKU库存] 获取分布式锁失败 - skuId: {}, quantity: {}, operation: deductStock", skuId, quantity);
            return false;
        }
        
        try {
            ProductSku sku = getById(skuId);
            if (sku == null) {
                log.warn("[SKU库存] SKU不存在 - skuId: {}, operation: deductStock", skuId);
                return false;
            }
            if (sku.getStock() < quantity) {
                log.warn("[SKU库存] 库存不足 - skuId: {}, currentStock: {}, requestQuantity: {}, operation: deductStock", 
                        skuId, sku.getStock(), quantity);
                return false;
            }
            
            sku.setStock(sku.getStock() - quantity);
            boolean result = updateById(sku);
            log.info("[SKU库存] 扣减成功 - skuId: {}, deductedQuantity: {}, remainingStock: {}", 
                    skuId, quantity, sku.getStock());
            return result;
        } finally {
            redisDistributedLock.releaseLock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStock(Long skuId, Integer quantity) {
        String lockKey = "sku_stock_lock:" + skuId;
        String lockValue = redisDistributedLock.tryLockWithWait(lockKey, 3, 10, TimeUnit.SECONDS);
        
        if (lockValue == null) {
            log.warn("[SKU库存] 获取分布式锁失败 - skuId: {}, quantity: {}, operation: addStock", skuId, quantity);
            return false;
        }
        
        try {
            ProductSku sku = getById(skuId);
            if (sku == null) {
                log.warn("[SKU库存] SKU不存在 - skuId: {}, operation: addStock", skuId);
                return false;
            }
            
            sku.setStock(sku.getStock() + quantity);
            boolean result = updateById(sku);
            log.info("[SKU库存] 增加成功 - skuId: {}, addedQuantity: {}, currentStock: {}", 
                    skuId, quantity, sku.getStock());
            return result;
        } finally {
            redisDistributedLock.releaseLock(lockKey, lockValue);
        }
    }

    @Override
    public void deleteByProductId(Long productId) {
        remove(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
    }
}
