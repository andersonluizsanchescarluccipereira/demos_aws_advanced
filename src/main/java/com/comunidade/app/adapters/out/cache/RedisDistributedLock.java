package com.comunidade.app.adapters.out.cache;

import com.comunidade.app.application.ports.out.DistributedLockPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDistributedLock implements DistributedLockPort {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisDistributedLock(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean acquireLock(String lockKey, String lockValue, long timeoutSeconds) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void releaseLock(String lockKey, String lockValue) {
        try {
            String currentValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            // Fallback: attempt to delete anyway
            try {
                redisTemplate.delete(lockKey);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Optional<String> getLock(String lockKey) {
        try {
            String value = redisTemplate.opsForValue().get(lockKey);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
