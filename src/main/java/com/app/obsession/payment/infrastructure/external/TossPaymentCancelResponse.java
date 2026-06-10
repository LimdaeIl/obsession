package com.app.obsession.payment.infrastructure.external;

public record TossPaymentCancelResponse(
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount,
        Long balanceAmount,
        String requestedAt,
        String approvedAt
) {
}
