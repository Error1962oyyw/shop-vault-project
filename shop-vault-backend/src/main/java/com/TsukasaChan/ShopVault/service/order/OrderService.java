package com.TsukasaChan.ShopVault.service.order;

import com.TsukasaChan.ShopVault.entity.order.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface OrderService extends IService<Order> {

    String buyNow(Long userId, Long productId, Integer quantity, Long userCouponId);

    String cartCheckout(Long userId, List<Long> cartItemIds, Long userCouponId);

    void payOrder(String orderNo, Long userId);

    void shipOrder(String orderNo);

    void confirmReceive(String orderNo, Long userId);

    void extendReceiveTime(String orderNo, Long userId);

    void cancelOrder(String orderNo, Long userId);
}
