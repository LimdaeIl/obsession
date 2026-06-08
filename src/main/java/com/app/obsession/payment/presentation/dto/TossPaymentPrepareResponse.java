package com.app.obsession.payment.presentation.dto;

public record TossPaymentPrepareResponse(
        String orderId,
        Long amount,
        String orderName
) {

}
