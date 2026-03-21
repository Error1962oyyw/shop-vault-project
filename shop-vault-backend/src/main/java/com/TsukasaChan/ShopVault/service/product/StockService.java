package com.TsukasaChan.ShopVault.service.product;

import java.util.Map;

public interface StockService {

    boolean deductStock(Long productId, Integer quantity);

    boolean isDeductStockFailed(Long productId, Integer quantity);

    boolean deductStockBatch(Map<Long, Integer> productQuantities);

    void restoreStock(Long productId, Integer quantity);
}
