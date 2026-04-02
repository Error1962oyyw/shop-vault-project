package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailDto {

    private Long id;

    private String orderNo;

    private Integer orderType;

    private String orderTypeName;

    private Integer status;

    private String statusName;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer pointsAmount;

    private String paymentMethod;

    private String paymentMethodName;

    private LocalDateTime expireTime;

    private LocalDateTime paymentTime;

    private LocalDateTime createTime;

    private LocalDateTime closeTime;

    private String closeReason;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String trackingCompany;

    private String trackingNo;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal productPrice;

    private String remark;
}
