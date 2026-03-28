package com.TsukasaChan.ShopVault.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Scanner;
import java.util.Random;

public class PasswordGenerator {
    
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    
    private static final String ILLEGAL_USERNAME_CHARS = " \t\n\r\"'\\<>&|";
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt密码生成工具 ===\n");
        
        if (args.length >= 2) {
            String username = args[0] != null ? args[0].trim() : "";
            String password = args[1] != null ? args[1].trim() : "";
            
            if (!validateCredentials(username, password)) {
                return;
            }
            
            generateAndPrint(encoder, username, password);
        } else if (args.length == 1) {
            if (args[0] != null && args[0].trim().equals("-i")) {
                interactiveMode(encoder);
            } else if (args[0] != null && args[0].trim().equals("-g")) {
                generatePasswordMode(encoder);
            } else {
                printUsage();
            }
        } else {
            printUsage();
            System.out.println("\n--- 预设用户密码 ---");
            String[][] credentials = {
                {"admin", "admin123"},
                {"test", "123456"},
                {"user", "123"}
            };
            for (String[] cred : credentials) {
                generateAndPrint(encoder, cred[0], cred[1]);
            }
        }
    }
    
    private static boolean validateCredentials(String username, String password) {
        if (username == null || username.isEmpty()) {
            System.out.println("错误：用户名和密码不能为空");
            return false;
        }
        
        if (password == null || password.isEmpty()) {
            System.out.println("错误：用户名和密码不能为空");
            return false;
        }
        
        for (char c : ILLEGAL_USERNAME_CHARS.toCharArray()) {
            if (username.indexOf(c) >= 0) {
                System.out.println("错误：用户名包含非法字符 '" + c + "'");
                return false;
            }
        }
        
        return true;
    }
    
    private static void interactiveMode(BCryptPasswordEncoder encoder) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("交互模式（输入 q 退出）\n");
        
        while (true) {
            System.out.print("请输入用户名: ");
            String username = scanner.nextLine().trim();
            if (username.equals("q")) break;
            
            System.out.print("请输入密码: ");
            String password = scanner.nextLine().trim();
            if (password.equals("q")) break;
            
            if (!validateCredentials(username, password)) {
                System.out.println();
                continue;
            }
            
            generateAndPrint(encoder, username, password);
            System.out.println();
        }
        
        scanner.close();
        System.out.println("已退出。");
    }
    
    private static void generatePasswordMode(BCryptPasswordEncoder encoder) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("密码生成模式（输入 q 退出）\n");
        
        while (true) {
            System.out.print("请输入用户名: ");
            String username = scanner.nextLine().trim();
            if (username.equals("q")) break;
            
            if (username.isEmpty()) {
                System.out.println("错误：用户名不能为空\n");
                continue;
            }
            
            System.out.println("\n请选择密码组成选项：");
            System.out.println("  1. 包含大写字母 (A-Z)");
            System.out.println("  2. 包含小写字母 (a-z)");
            System.out.println("  3. 包含数字 (0-9)");
            System.out.println("  4. 包含特殊符号 (!@#$%^&*等)");
            System.out.println();
            System.out.print("请输入选项编号（可多选，如 1234 或 13）: ");
            String options = scanner.nextLine().trim();
            if (options.equals("q")) break;
            
            System.out.print("请输入密码长度（默认12位）: ");
            String lengthInput = scanner.nextLine().trim();
            if (lengthInput.equals("q")) break;
            
            int length = 12;
            try {
                length = Integer.parseInt(lengthInput);
                if (length < 4) {
                    System.out.println("警告：密码长度过短，已调整为最小值 4");
                    length = 4;
                } else if (length > 128) {
                    System.out.println("警告：密码长度过长，已调整为最大值 128");
                    length = 128;
                }
            } catch (NumberFormatException e) {
                System.out.println("使用默认长度: 12");
            }
            
            String generatedPassword = generateRandomPassword(options, length);
            if (generatedPassword == null) {
                System.out.println("错误：请至少选择一个密码组成选项\n");
                continue;
            }
            
            System.out.println();
            generateAndPrint(encoder, username, generatedPassword);
            System.out.println();
        }
        
        scanner.close();
        System.out.println("已退出。");
    }
    
    static String generateRandomPassword(String options, int length) {
        StringBuilder charPool = new StringBuilder();
        
        if (options.contains("1")) {
            charPool.append(UPPERCASE);
        }
        if (options.contains("2")) {
            charPool.append(LOWERCASE);
        }
        if (options.contains("3")) {
            charPool.append(DIGITS);
        }
        if (options.contains("4")) {
            charPool.append(SPECIAL);
        }
        
        if (charPool.length() == 0) {
            return null;
        }
        
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charPool.length());
            password.append(charPool.charAt(index));
        }
        
        return password.toString();
    }
    
    private static void generateAndPrint(BCryptPasswordEncoder encoder, String username, String password) {
        String encoded = encoder.encode(password);
        System.out.println("-- 用户: " + username + ", 密码: " + password);
        System.out.println("BCrypt: " + encoded);
        System.out.println();
    }
    
    private static void printUsage() {
        System.out.println("用法:");
        System.out.println("  1. 生成指定用户密码:");
        System.out.println("     java PasswordGenerator <用户名> <密码>");
        System.out.println();
        System.out.println("  2. 交互模式:");
        System.out.println("     java PasswordGenerator -i");
        System.out.println();
        System.out.println("  3. 密码生成模式（自动生成随机密码）:");
        System.out.println("     java PasswordGenerator -g");
        System.out.println();
        System.out.println("  4. 显示预设用户密码:");
        System.out.println("     java PasswordGenerator");
    }
}
