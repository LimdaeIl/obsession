package com.app.obsession.payment.infrastructure.external;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String method,
        String status,
        Long totalAmount,
        String requestedAt,
        String approvedAt
) {

}
