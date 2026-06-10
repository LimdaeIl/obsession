package com.app.obsession.global.idempotency;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class IdempotencyCleanupScheduler {

    private final IdempotencyRepository idempotencyRepository;
    private final Clock clock;

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void deleteExpiredRecords() {
        idempotencyRepository.deleteExpired(LocalDateTime.now(clock));
    }
}
