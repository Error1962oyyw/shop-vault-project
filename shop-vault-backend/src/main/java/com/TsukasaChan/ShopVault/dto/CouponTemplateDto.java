package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponTemplateDto {

    private String name;

    private Integer type;

    private BigDecimal value;

    private BigDecimal discount;

    private BigDecimal minAmount;

    private BigDecimal maxDiscount;

    private Integer totalCount;

    private Integer perLimit;

    private Integer scopeType;

    private String scopeIds;

    private Integer validType;

    private LocalDateTime validStartTime;

    private LocalDateTime validEndTime;

    private Integer validDays;

    private Integer status;
}
