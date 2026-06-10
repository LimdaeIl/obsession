package com.app.obsession.global.outbox;

import com.app.obsession.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
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

    private OutboxEvent(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
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
}
