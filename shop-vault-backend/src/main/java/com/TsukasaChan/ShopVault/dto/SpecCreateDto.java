package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.util.List;

@Data
public class SpecCreateDto {

    private String name;

    private Integer sortOrder;

    private List<String> values;
}
