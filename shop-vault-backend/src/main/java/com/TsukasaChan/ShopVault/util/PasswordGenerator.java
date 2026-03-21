package com.TsukasaChan.ShopVault.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt密码生成工具 ===\n");
        
        String[][] credentials = {
            {"admin", "admin123"},
            {"test", "123456"},
            {"user", "123"}
        };
        
        for (String[] cred : credentials) {
            String username = cred[0];
            String password = cred[1];
            String encoded = encoder.encode(password);
            System.out.println("-- 用户: " + username + ", 密码: " + password);
            System.out.println("BCrypt: " + encoded);
            System.out.println();
        }
    }
}
