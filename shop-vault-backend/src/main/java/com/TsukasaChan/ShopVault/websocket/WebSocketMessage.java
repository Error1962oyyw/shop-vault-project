package com.TsukasaChan.ShopVault.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {

    private String type;

    private String title;

    private String content;

    private T data;

    private LocalDateTime timestamp;

    public static <T> WebSocketMessage<T> of(String type, String title, String content, T data) {
        return new WebSocketMessage<>(type, title, content, data, LocalDateTime.now());
    }

    public static WebSocketMessage<StockWarningData> stockWarning(Long productId, String productName, Integer stock) {
        return of("STOCK_WARNING", "库存预警", 
                "商品【" + productName + "】库存不足，当前库存：" + stock, 
                new StockWarningData(productId, productName, stock));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockWarningData {
        private Long productId;
        private String productName;
        private Integer stock;
    }
}
