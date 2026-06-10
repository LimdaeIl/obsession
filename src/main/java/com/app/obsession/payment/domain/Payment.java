package com.app.obsession.payment.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
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

    @Column(name = "payment_key", unique = true, length = 200)
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
            BigDecimal amount
    ) {
        validateReady(orderId, tossOrderId, amount);

        this.orderId = orderId;
        this.tossOrderId = tossOrderId;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public static Payment ready(
            Long orderId,
            String tossOrderId,
            BigDecimal amount
    ) {
        return new Payment(orderId, tossOrderId, amount);
    }

    public void approve(
            String paymentKey,
            String method
    ) {
        if (this.status != PaymentStatus.READY) {
            throw new PaymentException(PaymentErrorCode.ONLY_READY_PAYMENT_CAN_BE_APPROVED);
        }
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_KEY);
        }

        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.APPROVED;
    }

    public void fail() {
        if (this.status != PaymentStatus.READY) {
            throw new PaymentException(PaymentErrorCode.ONLY_READY_PAYMENT_CAN_BE_FAILED);
        }

        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        if (this.status != PaymentStatus.APPROVED) {
            throw new PaymentException(PaymentErrorCode.ONLY_APPROVED_PAYMENT_CAN_BE_CANCELED);
        }

        this.status = PaymentStatus.CANCELED;
    }

    public boolean isReady() {
        return this.status == PaymentStatus.READY;
    }

    public boolean isApproved() {
        return this.status == PaymentStatus.APPROVED;
    }

    private void validateReady(
            Long orderId,
            String tossOrderId,
            BigDecimal amount
    ) {
        if (orderId == null || orderId <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_ORDER_ID);
        }
        if (tossOrderId == null || tossOrderId.isBlank()) {
            throw new PaymentException(PaymentErrorCode.INVALID_ORDER_NUMBER);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }
}
