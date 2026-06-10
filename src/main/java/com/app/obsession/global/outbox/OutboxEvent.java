package com.app.obsession.global.outbox;

import com.app.obsession.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    private OutboxEvent(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public static OutboxEvent pending(String eventType, String payload) {
        return new OutboxEvent(eventType, payload);
    }

    public void markProcessed() {
        this.status = OutboxStatus.PROCESSED;
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == OutboxStatus.PENDING;
    }

    public boolean canRetry(int maxRetryCount) {
        return this.retryCount < maxRetryCount;
    }

    public void retryLater(
            LocalDateTime nextRetryAt,
            String errorMessage
    ) {
        this.retryCount++;
        this.nextRetryAt = nextRetryAt;
        this.lastErrorMessage = errorMessage == null
                ? null
                : errorMessage.substring(0, Math.min(errorMessage.length(), 1000));
    }

    public boolean isRetryDue(LocalDateTime now) {
        return nextRetryAt == null || !nextRetryAt.isAfter(now);
    }
}
