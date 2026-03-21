package com.TsukasaChan.ShopVault.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Component
public class RedisDistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    private static final long INITIAL_RETRY_INTERVAL_MS = 50;
    private static final long MAX_RETRY_INTERVAL_MS = 500;

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String tryLockWithWait(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        long startTime = System.nanoTime();
        String lockValue = UUID.randomUUID().toString();
        String key = LOCK_PREFIX + lockKey;
        long waitTimeNs = unit.toNanos(waitTime);
        long leaseTimeMs = unit.toMillis(leaseTime);
        int retryCount = 0;
        long currentIntervalMs = INITIAL_RETRY_INTERVAL_MS;
        
        while (System.nanoTime() - startTime < waitTimeNs) {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, lockValue, leaseTimeMs, TimeUnit.MILLISECONDS);
            
            if (Boolean.TRUE.equals(acquired)) {
                log.debug("[分布式锁] 获取锁成功(重试{}次) - key: {}, lockValue: {}, leaseTime: {}ms", 
                        retryCount, key, lockValue, leaseTimeMs);
                return lockValue;
            }
            
            retryCount++;
            
            long remainingWaitNs = waitTimeNs - (System.nanoTime() - startTime);
            if (remainingWaitNs <= 0) {
                break;
            }
            
            long sleepNs = Math.min(
                    TimeUnit.MILLISECONDS.toNanos(currentIntervalMs),
                    remainingWaitNs
            );
            LockSupport.parkNanos(sleepNs);
            
            if (Thread.currentThread().isInterrupted()) {
                log.warn("[分布式锁] 等待被中断 - key: {}, retryCount: {}", key, retryCount);
                Thread.currentThread().interrupt();
                return null;
            }
            
            currentIntervalMs = Math.min(currentIntervalMs * 2, MAX_RETRY_INTERVAL_MS);
        }
        
        log.warn("[分布式锁] 获取锁超时 - key: {}, waitTime: {}ms, retryCount: {}", key, unit.toMillis(waitTime), retryCount);
        return null;
    }

    public void releaseLock(String lockKey, String lockValue) {
        String key = LOCK_PREFIX + lockKey;
        
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), lockValue);
        
        if (Long.valueOf(1L).equals(result)) {
            log.debug("[分布式锁] 释放锁成功 - key: {}, lockValue: {}", key, lockValue);
        } else {
            log.warn("[分布式锁] 释放锁失败(锁不存在或已被其他线程持有) - key: {}, lockValue: {}", key, lockValue);
        }
    }
}
