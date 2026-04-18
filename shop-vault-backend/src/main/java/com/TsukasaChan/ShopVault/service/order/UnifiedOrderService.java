package com.TsukasaChan.ShopVault.service.order;

import com.TsukasaChan.ShopVault.dto.BuyNowDto;
import com.TsukasaChan.ShopVault.dto.CreateOrderDto;
import com.TsukasaChan.ShopVault.dto.OrderDetailDto;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UnifiedOrderService extends IService<Order> {

    Order createOrder(Long userId, CreateOrderDto dto);

    Order createVipOrder(Long userId, int vipType, String paymentMethod);

    Order createPointsExchangeOrder(Long userId, Long productId, Integer quantity);

    Order getOrderByNo(String orderNo);

    IPage<OrderDetailDto> getUserOrders(Long userId, Integer status, int page, int size, String keyword);

    OrderDetailDto getOrderDetail(Long userId, Long orderId);

    boolean payOrderByBalance(Long userId, Long orderId);

    boolean payOrderByDirect(Long userId, Long orderId);

    boolean payOrderByCombo(Long userId, Long orderId);

    boolean payOrderByPoints(Long userId, Long orderId);

    boolean cancelOrder(Long userId, Long orderId, String reason);

    void deleteOrder(Long userId, Long orderId);

    void confirmReceive(String orderNo, Long userId);

    void cancelExpiredOrders();

    void processOrderPayment(Long orderId, String paymentMethod);

    Order createOrderFromCart(Long userId, List<Long> cartItemIds, Long userCouponId);

    Order createBuyNowOrder(Long userId, BuyNowDto dto);

    List<Order> getExpiredOrders();
}
