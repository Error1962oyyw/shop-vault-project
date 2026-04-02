package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDto {

    private Integer orderType;

    private Long productId;

    private Long skuId;

    private Integer quantity;

    private Long addressId;

    private Long couponId;

    private Integer pointsUsed;

    private String paymentMethod;

    private String remark;

    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Long productId;
        private Long skuId;
        private Integer quantity;
    }
}
