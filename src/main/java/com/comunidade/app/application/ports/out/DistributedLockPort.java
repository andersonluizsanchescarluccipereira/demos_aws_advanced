package com.comunidade.app.application.ports.out;

import java.util.Optional;

public interface DistributedLockPort {
    boolean acquireLock(String lockKey, String lockValue, long timeoutSeconds);
    void releaseLock(String lockKey, String lockValue);
    Optional<String> getLock(String lockKey);
}

