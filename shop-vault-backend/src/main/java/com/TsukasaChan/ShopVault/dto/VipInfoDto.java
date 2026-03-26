package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VipInfoDto {
    private Integer vipLevel;
    private BigDecimal discountRate;
    private LocalDateTime vipExpireTime;
    private Integer totalVipDays;
    private Boolean isVip;
    private Integer remainingDays;
}
