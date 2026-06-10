package com.app.obsession.global.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class IdempotencyService {

    private static final int KEY_MAX_LENGTH = 100;
    private static final int TTL_HOURS = 24;

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public <T> T execute(
            String idempotencyKey,
            Object request,
            Class<T> responseType,
            Supplier<T> supplier
    ) {
        validateKey(idempotencyKey);

        String requestHash = hash(request);
        LocalDateTime now = LocalDateTime.now(clock);

        return idempotencyRepository.findByKey(idempotencyKey)
                .map(record -> handleExistingRecord(record, requestHash, now, responseType))
                .orElseGet(() -> executeNewRequest(
                        idempotencyKey,
                        requestHash,
                        responseType,
                        supplier,
                        now
                ));
    }

    private <T> T handleExistingRecord(
            IdempotencyRecord record,
            String requestHash,
            LocalDateTime now,
            Class<T> responseType
    ) {
        if (record.isExpired(now)) {
            throw new IdempotencyException(IdempotencyErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        if (record.isRequestHashDifferent(requestHash)) {
            throw new IdempotencyException(IdempotencyErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }

        if (record.isProcessing()) {
            throw new IdempotencyException(IdempotencyErrorCode.IDEMPOTENCY_REQUEST_PROCESSING);
        }

        if (record.isCompleted()) {
            return deserialize(record.getResponseBody(), responseType);
        }

        if (record.isCompleted()) {
            return deserialize(record.getResponseBody(), responseType);
        }

        if (record.isFailed()) {
            throw new IdempotencyException(IdempotencyErrorCode.IDEMPOTENCY_REQUEST_FAILED);
        }

        throw new IdempotencyException(IdempotencyErrorCode.IDEMPOTENCY_REQUEST_PROCESSING);
    }

    private <T> T executeNewRequest(
            String idempotencyKey,
            String requestHash,
            Class<T> responseType,
            Supplier<T> supplier,
            LocalDateTime now
    ) {
        IdempotencyRecord record = IdempotencyRecord.processing(
                idempotencyKey,
                requestHash,
                now.plusHours(TTL_HOURS)
        );

        try {
            idempotencyRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyException(IdempotencyErrorCode.IDEMPOTENCY_REQUEST_PROCESSING);
        }

        try {
            T response = supplier.get();
            record.complete(serialize(response));
            return response;
        } catch (RuntimeException e) {
            record.fail();
            throw e;
        }
    }

    private void validateKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyException(IdempotencyErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        if (idempotencyKey.length() > KEY_MAX_LENGTH) {
            throw new IdempotencyException(IdempotencyErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private String hash(Object request) {
        String json = serialize(request);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash idempotency request.", e);
        }
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotency object.", e);
        }
    }

    private <T> T deserialize(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize idempotency response.", e);
        }
    }
}
