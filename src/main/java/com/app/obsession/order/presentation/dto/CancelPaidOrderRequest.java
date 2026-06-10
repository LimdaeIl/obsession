package com.app.obsession.order.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelPaidOrderRequest(

        @NotBlank
        String cancelReason

) {
}
