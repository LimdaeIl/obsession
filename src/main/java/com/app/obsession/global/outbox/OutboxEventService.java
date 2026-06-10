package com.app.obsession.global.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePending(String eventType, String payload) {
        outboxEventRepository.save(OutboxEvent.pending(eventType, payload));
    }
}
