package com.app.obsession.payment.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.payment.application.ConfirmTossPaymentService;
import com.app.obsession.payment.application.PrepareTossPaymentService;
import com.app.obsession.payment.presentation.dto.TossPaymentConfirmRequest;
import com.app.obsession.payment.presentation.dto.TossPaymentPrepareResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
public class PaymentController {

    private final ConfirmTossPaymentService confirmTossPaymentService;
    private final PrepareTossPaymentService prepareTossPaymentService;

    @PostMapping("/toss/confirm")
    public CommonResponse<Long> confirm(
            @Valid @RequestBody TossPaymentConfirmRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long paymentId = confirmTossPaymentService.confirm(
                userDetails.getMemberId(),
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        return CommonResponse.success(
                "결제 승인에 성공했습니다.",
                paymentId
        );
    }

    @PostMapping("/toss/prepare/{orderId}")
    public CommonResponse<TossPaymentPrepareResponse> prepare(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TossPaymentPrepareResponse response = prepareTossPaymentService.prepare(
                userDetails.getMemberId(),
                orderId
        );

        return CommonResponse.success(
                "토스 결제 준비에 성공했습니다.",
                response
        );
    }
}
