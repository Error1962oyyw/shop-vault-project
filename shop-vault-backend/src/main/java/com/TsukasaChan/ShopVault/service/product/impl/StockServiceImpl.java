package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.common.RedisDistributedLock;
import com.TsukasaChan.ShopVault.service.product.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StockServiceImpl implements StockService {

    private final RedisDistributedLock distributedLock;
    private final StockTransactionalHelper transactionalHelper;

    private static final String STOCK_LOCK_KEY = "stock_lock:";
    private static final long LOCK_WAIT_TIME = 3000;
    private static final long LOCK_LEASE_TIME = 10000;

    public StockServiceImpl(RedisDistributedLock distributedLock, StockTransactionalHelper transactionalHelper) {
        this.distributedLock = distributedLock;
        this.transactionalHelper = transactionalHelper;
    }

    @Override
    public boolean deductStock(Long productId, Integer quantity) {
        return transactionalHelper.deductStockTransactional(productId, quantity);
    }

    @Override
    public boolean isDeductStockFailed(Long productId, Integer quantity) {
        String lockValue = distributedLock.tryLockWithWait(
                STOCK_LOCK_KEY + productId, 
                LOCK_WAIT_TIME, 
                LOCK_LEASE_TIME, 
                TimeUnit.MILLISECONDS);

        if (lockValue == null) {
            log.warn("[库存服务] 获取库存锁超时 - productId: {}", productId);
            return true;
        }

        try {
            return !deductStock(productId, quantity);
        } finally {
            distributedLock.releaseLock(STOCK_LOCK_KEY + productId, lockValue);
        }
    }

    @Override
    public boolean deductStockBatch(Map<Long, Integer> productQuantities) {
        if (productQuantities == null || productQuantities.isEmpty()) {
            return true;
        }

        List<Long> deductedProducts = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            if (isDeductStockFailed(productId, quantity)) {
                log.warn("[库存服务] 批量扣减失败，开始回滚 - 失败商品ID: {}", productId);
                for (Long restoredProductId : deductedProducts) {
                    try {
                        restoreStock(restoredProductId, productQuantities.get(restoredProductId));
                        log.info("[库存服务] 回滚库存成功 - productId: {}", restoredProductId);
                    } catch (Exception e) {
                        log.error("[库存服务] 回滚库存失败 - productId: {}", restoredProductId, e);
                    }
                }
                return false;
            }
            deductedProducts.add(productId);
        }
        
        log.info("[库存服务] 批量扣减成功 - 商品数量: {}", productQuantities.size());
        return true;
    }

    @Override
    public void restoreStock(Long productId, Integer quantity) {
        transactionalHelper.restoreStockTransactional(productId, quantity);
    }
}
