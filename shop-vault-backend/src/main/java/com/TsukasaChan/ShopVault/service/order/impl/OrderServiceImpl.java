package com.TsukasaChan.ShopVault.service.order.impl;

import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.mapper.order.OrderMapper;
import com.TsukasaChan.ShopVault.service.order.OrderCreationService;
import com.TsukasaChan.ShopVault.service.order.OrderLifecycleService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderCreationService orderCreationService;
    private final OrderLifecycleService orderLifecycleService;

    public OrderServiceImpl(OrderCreationService orderCreationService, OrderLifecycleService orderLifecycleService) {
        this.orderCreationService = orderCreationService;
        this.orderLifecycleService = orderLifecycleService;
    }

    @Override
    public String buyNow(Long userId, Long productId, Integer quantity, Long userCouponId) {
        return orderCreationService.buyNow(userId, productId, quantity, userCouponId);
    }

    @Override
    public String cartCheckout(Long userId, List<Long> cartItemIds, Long userCouponId) {
        return orderCreationService.cartCheckout(userId, cartItemIds, userCouponId);
    }

    @Override
    public void payOrder(String orderNo, Long userId) {
        throw new UnsupportedOperationException("请使用 UnifiedOrderService 的支付接口");
    }

    @Override
    public void shipOrder(String orderNo) {
        orderLifecycleService.shipOrder(orderNo);
    }

    @Override
    public void confirmReceive(String orderNo, Long userId) {
        orderLifecycleService.confirmReceive(orderNo, userId);
    }

    @Override
    public void extendReceiveTime(String orderNo, Long userId) {
        orderLifecycleService.extendReceiveTime(orderNo, userId);
    }

    @Override
    public void cancelOrder(String orderNo, Long userId) {
        orderLifecycleService.cancelOrder(orderNo, userId);
    }
}
