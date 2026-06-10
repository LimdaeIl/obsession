package com.app.obsession.global.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OutboxEventProcessingScheduler {

    private final OutboxEventProcessingService outboxEventProcessingService;

    @Scheduled(fixedDelay = 60_000)
    public void processPendingEvents() {
        outboxEventProcessingService.processPendingEvents();
    }
}
