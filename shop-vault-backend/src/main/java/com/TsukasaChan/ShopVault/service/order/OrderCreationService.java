package com.TsukasaChan.ShopVault.service.order;

import java.util.List;

public interface OrderCreationService {

    String buyNow(Long userId, Long productId, Integer quantity, Long userCouponId);

    String cartCheckout(Long userId, List<Long> cartItemIds, Long userCouponId);
}
