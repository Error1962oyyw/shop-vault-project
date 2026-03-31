package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductSpecDto {

    private Long id;

    private Long specId;

    private String specName;

    private Integer sortOrder;

    private Integer isCustom;

    private List<ProductSpecValueDto> values;
}
