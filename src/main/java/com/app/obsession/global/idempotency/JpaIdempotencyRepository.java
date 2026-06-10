package com.app.obsession.global.idempotency;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JpaIdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {

    @Modifying
    @Query("delete from IdempotencyRecord r where r.expiredAt < :now")
    int deleteExpired(LocalDateTime now);
    
}
