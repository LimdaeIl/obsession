package com.app.obsession.payment.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "toss_order_id", nullable = false, unique = true, length = 64)
    private String tossOrderId;

    @Column(name = "payment_key", nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "method", length = 30)
    private String method;

    private Payment(
            Long orderId,
            String tossOrderId,
            String paymentKey,
            BigDecimal amount,
            PaymentStatus status,
            String method
    ) {
        this.orderId = orderId;
        this.tossOrderId = tossOrderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
        this.method = method;
    }

    public static Payment paid(
            Long orderId,
            String tossOrderId,
            String paymentKey,
            BigDecimal amount,
            String method
    ) {
        return new Payment(
                orderId,
                tossOrderId,
                paymentKey,
                amount,
                PaymentStatus.PAID,
                method
        );
    }
}
