package com.TsukasaChan.ShopVault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shop-vault.points")
public class PointsConfig {

    private int expireDays = 365;

    private int pointsToCashRate = 100;

    private int minUsePoints = 100;

    private int maxUsePercent = 50;
}
