package com.TsukasaChan.ShopVault.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberDayDto {

    private Long id;

    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal discountRate;

    private BigDecimal pointsMultiplier;

    private String ruleExpression;

    private Boolean isActive;

    private String description;
}
