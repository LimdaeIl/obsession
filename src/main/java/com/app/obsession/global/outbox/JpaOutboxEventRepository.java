package com.app.obsession.global.outbox;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            select e
            from OutboxEvent e
            where e.status = :status
              and (e.nextRetryAt is null or e.nextRetryAt <= :now)
            order by e.id asc
            """)
    List<OutboxEvent> findRetryDuePendingEvents(
            @Param("status") OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
