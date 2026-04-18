package com.comunidade.app.adapters.out.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisDistributedLockTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisDistributedLock lock;

    @BeforeEach
    void setUp() {
        lock = new RedisDistributedLock(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testAcquireLockSuccess() {
        when(valueOperations.setIfAbsent("lock-key", "lock-value", 10, TimeUnit.SECONDS))
                .thenReturn(true);

        boolean result = lock.acquireLock("lock-key", "lock-value", 10);

        assertTrue(result);
    }

    @Test
    void testAcquireLockFailure() {
        when(valueOperations.setIfAbsent("lock-key", "lock-value", 10, TimeUnit.SECONDS))
                .thenReturn(false);

        boolean result = lock.acquireLock("lock-key", "lock-value", 10);

        assertFalse(result);
    }

    @Test
    void testAcquireLockException() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException("Redis error"));

        boolean result = lock.acquireLock("lock-key", "lock-value", 10);

        assertFalse(result);
    }

    @Test
    void testReleaseLock() {
        when(valueOperations.get("lock-key")).thenReturn("lock-value");
        when(redisTemplate.delete("lock-key")).thenReturn(true);

        lock.releaseLock("lock-key", "lock-value");

        assertTrue(true);
    }

    @Test
    void testReleaseLockWithDifferentValue() {
        when(valueOperations.get("lock-key")).thenReturn("other-value");

        lock.releaseLock("lock-key", "lock-value");

        assertTrue(true);
    }

    @Test
    void testGetLockExists() {
        when(valueOperations.get("lock-key")).thenReturn("lock-value");

        Optional<String> result = lock.getLock("lock-key");

        assertTrue(result.isPresent());
        assertEquals("lock-value", result.get());
    }

    @Test
    void testGetLockNotExists() {
        when(valueOperations.get("lock-key")).thenReturn(null);

        Optional<String> result = lock.getLock("lock-key");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetLockException() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        Optional<String> result = lock.getLock("lock-key");

        assertFalse(result.isPresent());
    }
}

