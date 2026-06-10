package com.app.obsession.global.outbox;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OutboxEventRepository {

    private static final int PROCESSING_BATCH_SIZE = 50;

    private final JpaOutboxEventRepository jpaOutboxEventRepository;

    public OutboxEvent save(OutboxEvent event) {
        return jpaOutboxEventRepository.save(event);
    }

    public List<OutboxEvent> findRetryDuePendingEvents(LocalDateTime now) {
        return jpaOutboxEventRepository.findRetryDuePendingEvents(
                OutboxStatus.PENDING,
                now,
                PageRequest.of(0, PROCESSING_BATCH_SIZE)
        );
    }
}
