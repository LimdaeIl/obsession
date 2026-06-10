package com.app.obsession.global.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop20ByStatusOrderByIdAsc(OutboxStatus status);
}
