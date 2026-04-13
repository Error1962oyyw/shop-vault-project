package com.TsukasaChan.ShopVault.util;

import cn.hutool.core.util.IdUtil;

public final class OrderNoGenerator {

    private OrderNoGenerator() {
    }

    public static String generate() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    public static String generateWithPrefix(String prefix) {
        return prefix + generate();
    }

    public static String generatePaymentNo() {
        return "PAY" + generate();
    }
}
