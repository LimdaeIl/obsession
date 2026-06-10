package com.app.obsession.global.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OutboxEventRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;

    public OutboxEvent save(OutboxEvent event) {
        return jpaOutboxEventRepository.save(event);
    }

    public List<OutboxEvent> findPendingEvents() {
        return jpaOutboxEventRepository.findTop20ByStatusOrderByIdAsc(OutboxStatus.PENDING);
    }
}
