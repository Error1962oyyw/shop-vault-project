package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

@Data
public class ProductSpecValueDto {

    private Long id;

    private Long specValueId;

    private String value;

    private Integer sortOrder;

    private Integer isCustom;
}
