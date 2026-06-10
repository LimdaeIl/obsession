package com.app.obsession.global.idempotency;

import com.app.obsession.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_idempotency_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class IdempotencyRecord extends BaseTimeEntity {

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    private IdempotencyRecord(
            String idempotencyKey,
            String requestHash,
            LocalDateTime expiredAt
    ) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = IdempotencyStatus.PROCESSING;
        this.expiredAt = expiredAt;
    }

    public static IdempotencyRecord processing(
            String idempotencyKey,
            String requestHash,
            LocalDateTime expiredAt
    ) {
        return new IdempotencyRecord(idempotencyKey, requestHash, expiredAt);
    }

    public void complete(String responseBody) {
        this.responseBody = responseBody;
        this.status = IdempotencyStatus.COMPLETED;
    }

    public void fail() {
        this.status = IdempotencyStatus.FAILED;
    }

    public boolean isCompleted() {
        return this.status == IdempotencyStatus.COMPLETED;
    }

    public boolean isProcessing() {
        return this.status == IdempotencyStatus.PROCESSING;
    }

    public boolean isRequestHashDifferent(String requestHash) {
        return !this.requestHash.equals(requestHash);
    }

    public boolean isExpired(LocalDateTime now) {
        return this.expiredAt.isBefore(now);
    }

    public boolean isFailed() {
        return this.status == IdempotencyStatus.FAILED;
    }
}
