package com.app.obsession.global.lock;

import com.app.obsession.global.exception.AppErrorCode;
import com.app.obsession.global.exception.AppException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedissonDistributedLockExecutor implements DistributedLockExecutor {

    private final RedissonClient redissonClient;

    @Override
    public <T> T execute(
            String lockKey,
            Duration waitTime,
            Duration leaseTime,
            Supplier<T> supplier
    ) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(
                    waitTime.toMillis(),
                    leaseTime.toMillis(),
                    TimeUnit.MILLISECONDS
            );

            if (!acquired) {
                throw new AppException(AppErrorCode.LOCK_ACQUISITION_FAILED);
            }

            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(AppErrorCode.LOCK_INTERRUPTED);

        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
