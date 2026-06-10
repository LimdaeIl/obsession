package com.app.obsession.global.outbox;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OutboxEventProcessingService {

    private final OutboxEventRepository outboxEventRepository;
    private final List<OutboxEventProcessor> processors;
    private static final int MAX_RETRY_COUNT = 5;
    private static final long RETRY_DELAY_MINUTES = 5;
    private final Clock clock;

    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents();

        LocalDateTime now = LocalDateTime.now(clock);

        for (OutboxEvent event : events) {
            if (!event.isRetryDue(now)) {
                continue;
            }

            process(event);
        }
    }

    private void process(OutboxEvent event) {
        OutboxEventProcessor processor = processors.stream()
                .filter(candidate -> candidate.supports(event.getEventType()))
                .findFirst()
                .orElse(null);

        if (processor == null) {
            log.warn("No outbox processor found. eventId={}, eventType={}",
                    event.getId(),
                    event.getEventType()
            );
            event.markFailed();
            return;
        }

        try {
            processor.process(event);
            event.markProcessed();
        } catch (RuntimeException e) {
            log.warn("Failed to process outbox event. eventId={}, eventType={}",
                    event.getId(),
                    event.getEventType(),
                    e
            );
            if (!event.canRetry(MAX_RETRY_COUNT)) {
                event.markFailed();
                return;
            }

            event.retryLater(
                    LocalDateTime.now(clock)
                            .plusMinutes(RETRY_DELAY_MINUTES),
                    e.getMessage()
            );
        }
    }
}
