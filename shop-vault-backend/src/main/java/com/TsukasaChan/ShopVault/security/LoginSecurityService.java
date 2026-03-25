package com.TsukasaChan.ShopVault.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSecurityService {

    private final StringRedisTemplate redisTemplate;
    
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_DURATION_MINUTES = 30;
    private static final long FAIL_COUNT_EXPIRE_HOURS = 24;

    public boolean isAccountLocked(String email) {
        String lockKey = LOGIN_LOCK_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public String getLockRemainingTime(String email) {
        String lockKey = LOGIN_LOCK_PREFIX + email;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        if (ttl != null && ttl > 0) {
            return ttl + "分钟";
        }
        return "";
    }

    public void recordLoginFailure(String email) {
        String failKey = LOGIN_FAIL_PREFIX + email;
        String lockKey = LOGIN_LOCK_PREFIX + email;
        
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount != null && failCount == 1) {
            redisTemplate.expire(failKey, FAIL_COUNT_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        
        log.warn("登录失败记录: email={}, 失败次数={}", email, failCount);
        
        if (failCount != null && failCount >= MAX_FAIL_COUNT) {
            redisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(failKey);
            log.warn("账户已锁定: email={}, 锁定时长={}分钟", email, LOCK_DURATION_MINUTES);
        }
    }

    public void recordLoginSuccess(String email) {
        String failKey = LOGIN_FAIL_PREFIX + email;
        redisTemplate.delete(failKey);
        log.info("登录成功，清除失败记录: email={}", email);
    }

    public int getRemainingAttempts(String email) {
        String failKey = LOGIN_FAIL_PREFIX + email;
        String countStr = redisTemplate.opsForValue().get(failKey);
        if (countStr == null) {
            return MAX_FAIL_COUNT;
        }
        int failCount = Integer.parseInt(countStr);
        return Math.max(0, MAX_FAIL_COUNT - failCount);
    }
}
