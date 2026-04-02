package com.TsukasaChan.ShopVault.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoGenerator() {
    }

    public static String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String random = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        return timestamp + random;
    }

    public static String generateWithPrefix(String prefix) {
        return prefix + generate();
    }

    public static String generatePaymentNo() {
        return "PAY" + generate();
    }
}
