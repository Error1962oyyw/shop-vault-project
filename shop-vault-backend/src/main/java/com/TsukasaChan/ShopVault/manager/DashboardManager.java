package com.TsukasaChan.ShopVault.manager;

import com.TsukasaChan.ShopVault.dto.DashboardStatsDto;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardManager {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final PointsRecordService pointsRecordService;

    public DashboardStatsDto getFullStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalOrders(orderService.count(new LambdaQueryWrapper<Order>()
                .ne(Order::getStatus, 0)));

        stats.setTotalAmount(calculateTotalAmount());

        stats.setTotalUsers(userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "USER")));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.setTodayOrders(orderService.count(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .ne(Order::getStatus, 0)));

        stats.setTodayAmount(calculateTodayAmount(todayStart));

        stats.setTodayNewUsers(userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart)
                .eq(User::getRole, "USER")));

        stats.setDau(calculateDAU(todayStart));
        stats.setWau(calculateWAU());
        stats.setMau(calculateMAU());

        stats.setTotalPointsIssued(calculateTotalPointsIssued());
        stats.setTotalPointsUsed(calculateTotalPointsUsed());

        stats.setHotProducts(getTop10Products());

        stats.setOrderTrend(getTrendData());
        stats.setUserTrend(getTrendData());

        return stats;
    }

    private BigDecimal calculateTotalAmount() {
        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, Arrays.asList(1, 2, 3)));
        return orders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTodayAmount(LocalDateTime todayStart) {
        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .in(Order::getStatus, Arrays.asList(1, 2, 3)));
        return orders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateDAU(LocalDateTime todayStart) {
        return (int) userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart));
    }

    private int calculateWAU() {
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        return (int) userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, weekStart));
    }

    private int calculateMAU() {
        LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();
        return (int) userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, monthStart));
    }

    private long calculateTotalPointsIssued() {
        List<PointsRecord> records = pointsRecordService.list(new LambdaQueryWrapper<PointsRecord>()
                .gt(PointsRecord::getPoints, 0));
        return records.stream().mapToLong(PointsRecord::getPoints).sum();
    }

    private long calculateTotalPointsUsed() {
        List<PointsRecord> records = pointsRecordService.list(new LambdaQueryWrapper<PointsRecord>()
                .lt(PointsRecord::getPoints, 0));
        return records.stream().mapToLong(r -> Math.abs(r.getPoints())).sum();
    }

    private List<DashboardStatsDto.TopProductDto> getTop10Products() {
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("LIMIT 10"));

        return products.stream().map(p -> {
            DashboardStatsDto.TopProductDto dto = new DashboardStatsDto.TopProductDto();
            dto.setProductId(p.getId());
            dto.setProductName(p.getName());
            dto.setProductImage(p.getMainImage());
            dto.setSales(p.getSales());
            dto.setAmount(p.getPrice().multiply(new BigDecimal(p.getSales())));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<DashboardStatsDto.TrendDataDto> getTrendData() {
        List<DashboardStatsDto.TrendDataDto> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            DashboardStatsDto.TrendDataDto dto = new DashboardStatsDto.TrendDataDto();
            dto.setDate(date.format(formatter));

            List<Order> dayOrders = orderService.list(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, start)
                    .le(Order::getCreateTime, end)
                    .ne(Order::getStatus, 0));

            dto.setCount((long) dayOrders.size());
            dto.setAmount(dayOrders.stream()
                    .map(Order::getPayAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            trend.add(dto);
        }

        return trend;
    }
}
