package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

@Data
public class OnboardingStatusDto {

    private Boolean isFirstLogin;

    private Boolean hasCompletedOnboarding;
}
