package com.TsukasaChan.ShopVault.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AfterSalesHandleDto {

    private String orderNo;

    private Boolean isAgree;

    private String reply;

    private BigDecimal refundAmount;

    private String adminRemark;
}
