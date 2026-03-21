package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSkuBatchDto {

    private Long productId;

    private List<SkuItem> skuList;

    @Data
    public static class SkuItem {

        private String skuCode;

        private String specJson;

        private BigDecimal price;

        private Integer stock;

        private Integer stockWarning;

        private String image;
    }
}
