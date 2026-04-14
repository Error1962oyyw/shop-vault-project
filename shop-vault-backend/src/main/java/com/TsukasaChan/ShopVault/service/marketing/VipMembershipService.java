package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.VipMembership;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VipMembershipService extends IService<VipMembership> {

    VipMembership getActiveVipByUserId(Long userId);

    boolean isVip(Long userId);

    void exchangeVip(Long userId, int vipType);

    void purchaseVip(Long userId, int vipType, String paymentMethod);

    void activateVipAfterOrderPayment(Long userId, int vipType);

    void checkAndExpireVip();

    List<VipMembership> getVipHistory(Long userId);
}
