package com.app.obsession.global.idempotency;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class IdempotencyTransactionService {

    private final IdempotencyRepository idempotencyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createProcessing(
            String idempotencyKey,
            String requestHash,
            LocalDateTime expiredAt
    ) {
        try {
            IdempotencyRecord record = IdempotencyRecord.processing(
                    idempotencyKey,
                    requestHash,
                    expiredAt
            );

            idempotencyRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyException(IdempotencyErrorCode.IDEMPOTENCY_REQUEST_PROCESSING);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(
            String idempotencyKey,
            String responseBody
    ) {
        IdempotencyRecord record = idempotencyRepository.findByKey(idempotencyKey)
                .orElseThrow(() -> new IdempotencyException(IdempotencyErrorCode.INVALID_IDEMPOTENCY_KEY));

        record.complete(responseBody);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String idempotencyKey) {
        IdempotencyRecord record = idempotencyRepository.findByKey(idempotencyKey)
                .orElseThrow(() -> new IdempotencyException(IdempotencyErrorCode.INVALID_IDEMPOTENCY_KEY));

        record.fail();
    }
}
