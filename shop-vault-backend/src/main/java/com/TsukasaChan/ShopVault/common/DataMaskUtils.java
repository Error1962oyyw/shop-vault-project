package com.TsukasaChan.ShopVault.common;

import org.springframework.stereotype.Component;

@Component
public class DataMaskUtils {

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String prefix = parts[0];
        String suffix = parts[1];
        
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***@" + suffix;
        }
        return prefix.substring(0, 2) + "***@" + suffix;
    }

    public static String maskAddress(String address) {
        if (address == null || address.length() < 10) {
            return address;
        }
        return address.substring(0, address.length() - 4) + "****";
    }

    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.substring(name.length() - 1);
    }
}
