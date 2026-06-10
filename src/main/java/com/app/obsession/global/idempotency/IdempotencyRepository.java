package com.app.obsession.global.idempotency;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class IdempotencyRepository {

    private final JpaIdempotencyRepository jpaIdempotencyRepository;

    public Optional<IdempotencyRecord> findByKey(String idempotencyKey) {
        return jpaIdempotencyRepository.findById(idempotencyKey);
    }

    public IdempotencyRecord save(IdempotencyRecord record) {
        return jpaIdempotencyRepository.save(record);
    }

    public IdempotencyRecord saveAndFlush(IdempotencyRecord record) {
        return jpaIdempotencyRepository.saveAndFlush(record);
    }

    public int deleteExpired(LocalDateTime now) {
        return jpaIdempotencyRepository.deleteExpired(now);
    }
}

