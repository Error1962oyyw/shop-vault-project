package com.TsukasaChan.ShopVault.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsDto {

    private Long totalOrders;

    private BigDecimal totalAmount;

    private Long totalUsers;

    private Long todayOrders;

    private BigDecimal todayAmount;

    private Long todayNewUsers;

    private Integer dau;

    private Integer wau;

    private Integer mau;

    private Long totalPointsIssued;

    private Long totalPointsUsed;

    private List<TopProductDto> hotProducts;

    private List<TrendDataDto> orderTrend;

    private List<TrendDataDto> userTrend;

    @Data
    public static class TopProductDto {

        private Long productId;

        private String productName;

        private String productImage;

        private Integer sales;

        private BigDecimal amount;
    }

    @Data
    public static class TrendDataDto {

        private String date;

        private Long count;

        private BigDecimal amount;
    }
}
