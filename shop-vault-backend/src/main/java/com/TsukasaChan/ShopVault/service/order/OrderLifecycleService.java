package com.TsukasaChan.ShopVault.service.order;

public interface OrderLifecycleService {

    void shipOrder(String orderNo);

    void confirmReceive(String orderNo, Long userId);

    void extendReceiveTime(String orderNo, Long userId);

    void cancelOrder(String orderNo, Long userId);
}
