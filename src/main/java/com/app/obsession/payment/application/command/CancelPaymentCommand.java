package com.app.obsession.payment.application.command;

public record CancelPaymentCommand(
        Long orderId,
        String paymentKey
) {

}
