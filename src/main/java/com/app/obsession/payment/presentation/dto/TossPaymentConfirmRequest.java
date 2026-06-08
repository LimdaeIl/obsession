package com.app.obsession.payment.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TossPaymentConfirmRequest(

        @NotBlank
        String paymentKey,

        @NotBlank
        String orderId,

        @NotNull
        Long amount
) {

}
