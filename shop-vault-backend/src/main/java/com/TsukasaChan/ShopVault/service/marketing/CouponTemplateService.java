package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.CouponTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CouponTemplateService extends IService<CouponTemplate> {

    List<CouponTemplate> listAvailable();

    boolean canReceive(Long templateId, Long userId);

    boolean receiveCoupon(Long templateId, Long userId);

    CouponTemplate getAvailableById(Long id);
}
