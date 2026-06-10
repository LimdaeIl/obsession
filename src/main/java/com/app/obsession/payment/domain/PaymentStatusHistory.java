package com.app.obsession.payment.domain;

import com.app.obsession.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_payment_status_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PaymentStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private PaymentStatus toStatus;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    private PaymentStatusHistory(
            Long paymentId,
            Long orderId,
            PaymentStatus fromStatus,
            PaymentStatus toStatus,
            String reason
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
    }

    public static PaymentStatusHistory record(
            Long paymentId,
            Long orderId,
            PaymentStatus fromStatus,
            PaymentStatus toStatus,
            String reason
    ) {
        return new PaymentStatusHistory(
                paymentId,
                orderId,
                fromStatus,
                toStatus,
                reason
        );
    }
}
