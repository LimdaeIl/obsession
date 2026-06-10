package com.app.obsession.payment.application;

import com.app.obsession.global.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConfirmTossPaymentService {

    private final IdempotencyService idempotencyService;
    private final ConfirmTossPaymentProcessor confirmTossPaymentProcessor;

    public Long confirm(
            String idempotencyKey,
            Long memberId,
            String paymentKey,
            String tossOrderId,
            Long amount
    ) {
        ConfirmPaymentIdempotencyRequest request = new ConfirmPaymentIdempotencyRequest(
                memberId,
                paymentKey,
                tossOrderId,
                amount
        );

        return idempotencyService.execute(
                idempotencyKey,
                request,
                Long.class,
                () -> confirmTossPaymentProcessor.confirm(
                        memberId,
                        paymentKey,
                        tossOrderId,
                        amount
                )
        );
    }

    private record ConfirmPaymentIdempotencyRequest(
            Long memberId,
            String paymentKey,
            String tossOrderId,
            Long amount
    ) {
    }
}
