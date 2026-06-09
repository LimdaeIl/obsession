package com.app.obsession.global.lock;

import java.time.Duration;
import java.util.function.Supplier;

public interface DistributedLockExecutor {

    <T> T execute(
            String lockKey,
            Duration waitTime,
            Duration leaseTime,
            Supplier<T> supplier
    );
}
