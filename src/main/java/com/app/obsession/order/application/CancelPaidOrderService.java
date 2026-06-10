package com.app.obsession.order.application;

import com.app.obsession.global.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CancelPaidOrderService {

    private final IdempotencyService idempotencyService;
    private final CancelPaidOrderProcessor cancelPaidOrderProcessor;

    public void cancel(
            String idempotencyKey,
            Long orderId,
            Long memberId,
            String cancelReason
    ) {
        CancelPaidOrderIdempotencyRequest request = new CancelPaidOrderIdempotencyRequest(
                orderId,
                memberId,
                cancelReason
        );

        idempotencyService.execute(
                idempotencyKey,
                request,
                VoidResponse.class,
                () -> {
                    cancelPaidOrderProcessor.cancel(orderId, memberId, cancelReason);
                    return VoidResponse.INSTANCE;
                }
        );
    }

    private record CancelPaidOrderIdempotencyRequest(
            Long orderId,
            Long memberId,
            String cancelReason
    ) {
    }

    private enum VoidResponse {
        INSTANCE
    }
}
