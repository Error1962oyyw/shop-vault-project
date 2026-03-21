package com.TsukasaChan.ShopVault.common;

import com.TsukasaChan.ShopVault.entity.marketing.CouponTemplate;

import java.math.BigDecimal;
import java.util.Arrays;

public final class CouponUtils {

    private CouponUtils() {
    }

    public static boolean isApplicable(CouponTemplate template, BigDecimal orderAmount, Long categoryId, Long productId) {
        if (template == null || orderAmount == null) {
            return false;
        }
        
        if (orderAmount.compareTo(template.getMinAmount()) < 0) {
            return false;
        }
        
        return switch (template.getScopeType()) {
            case 1 -> true;
            case 2 -> template.getScopeIds() != null && categoryId != null 
                    && Arrays.asList(template.getScopeIds().split(",")).contains(String.valueOf(categoryId));
            case 3 -> template.getScopeIds() != null && productId != null 
                    && Arrays.asList(template.getScopeIds().split(",")).contains(String.valueOf(productId));
            default -> false;
        };
    }
}
