package com.TsukasaChan.ShopVault.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AfterSalesApplyDto {

    private String orderNo;

    private Integer type;

    private String reason;

    private String description;

    private List<String> images;

    private BigDecimal refundAmount;
}
