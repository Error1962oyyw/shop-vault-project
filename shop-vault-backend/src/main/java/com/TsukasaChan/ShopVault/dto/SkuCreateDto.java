package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuCreateDto {

    private Long productId;

    private String skuCode;

    private String specJson;

    private BigDecimal price;

    private Integer stock;

    private Integer stockWarning;

    private String image;
}
