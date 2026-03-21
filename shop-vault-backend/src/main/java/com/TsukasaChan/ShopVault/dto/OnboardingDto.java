package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.util.List;

@Data
public class OnboardingDto {

    private List<Long> preferredCategoryIds;
}
