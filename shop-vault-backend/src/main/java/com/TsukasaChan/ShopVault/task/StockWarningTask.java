package com.TsukasaChan.ShopVault.task;

import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.websocket.WebSocketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockWarningTask {

    private final ProductService productService;
    private final WebSocketService webSocketService;

    @Scheduled(cron = "0 0 * * * ?")
    public void checkStockWarning() {
        log.info("开始检查库存预警...");
        
        List<Product> lowStockProducts = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .isNotNull(Product::getStockWarning)
                .apply("stock <= stock_warning"));
        
        for (Product product : lowStockProducts) {
            webSocketService.notifyStockWarning(
                    product.getId(),
                    product.getName(),
                    product.getStock()
            );
            log.warn("库存预警: 商品[{}]库存不足, 当前库存: {}", product.getName(), product.getStock());
        }
        
        log.info("库存预警检查完成, 发现{}个库存不足商品", lowStockProducts.size());
    }
}
