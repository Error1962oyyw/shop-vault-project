package com.TsukasaChan.ShopVault.infrastructure;

import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CODE_PREFIX = "verify:code:";
    private static final String RATE_LIMIT_PREFIX = "verify:rate:";
    private static final String DAILY_LIMIT_PREFIX = "verify:daily:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int RATE_LIMIT_SECONDS = 60;
    private static final int DAILY_LIMIT = 10;

    public void sendVerificationCode(String email) {
        String rateLimitKey = RATE_LIMIT_PREFIX + email;
        String dailyLimitKey = DAILY_LIMIT_PREFIX + email;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            throw new RuntimeException("请求过于频繁，请" + (ttl != null ? ttl : 60) + "秒后再试");
        }
        
        String dailyCountStr = redisTemplate.opsForValue().get(dailyLimitKey);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;
        if (dailyCount >= DAILY_LIMIT) {
            throw new RuntimeException("今日验证码发送次数已达上限，请明天再试");
        }

        String code = RandomUtil.randomNumbers(6);

        redisTemplate.opsForValue().set(Objects.requireNonNull(CODE_PREFIX + email), code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        
        if (dailyCount == 0) {
            redisTemplate.opsForValue().set(dailyLimitKey, "1", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(dailyLimitKey);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【小铺宝库】账号验证码");
            message.setText("亲爱的用户您好：\n\n您正在进行身份验证，您的验证码为：【 " + code + " 】。\n" +
                    "该验证码将在 5 分钟后失效。\n\n如非本人操作，请忽略此邮件。");

            mailSender.send(message);
            log.info("验证码已成功发送至邮箱: {}", email);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            redisTemplate.delete(CODE_PREFIX + email);
            redisTemplate.delete(rateLimitKey);
            throw new RuntimeException("邮件发送异常，请稍后再试");
        }
    }

    public boolean isCodeInvalid(String email, String inputCode) {
        if (email == null || inputCode == null) {
            return true;
        }
        String savedCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(CODE_PREFIX + email);
            return false;
        }
        return true;
    }
}
