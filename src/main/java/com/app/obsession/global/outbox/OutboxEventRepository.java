package com.app.obsession.global.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OutboxEventRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;

    public OutboxEvent save(OutboxEvent event) {
        return jpaOutboxEventRepository.save(event);
    }
}
