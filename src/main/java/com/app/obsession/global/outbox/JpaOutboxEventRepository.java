package com.app.obsession.global.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

}
