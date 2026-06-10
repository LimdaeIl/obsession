package com.app.obsession.global.outbox;

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

    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents();

        for (OutboxEvent event : events) {
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
            event.markFailed();
        }
    }
}
