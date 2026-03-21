package com.TsukasaChan.ShopVault.infrastructure;

import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CODE_PREFIX = "verify:code:";

    public void sendVerificationCode(String email) {
        String code = RandomUtil.randomNumbers(6);

        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, 5, TimeUnit.MINUTES);

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
