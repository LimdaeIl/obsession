package com.app.obsession.order.application;

import com.app.obsession.payment.application.command.CancelPaymentCommand;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import com.app.obsession.payment.infrastructure.external.TossPaymentCancelResponse;
import com.app.obsession.payment.infrastructure.external.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CancelPaidOrderProcessor {

    private final TossPaymentClient tossPaymentClient;
    private final CancelPaidOrderTransactionService cancelPaidOrderTransactionService;

    public void cancel(
            Long orderId,
            Long memberId,
            String cancelReason
    ) {
        CancelPaymentCommand command =
                cancelPaidOrderTransactionService.requestCancel(orderId, memberId);

        TossPaymentCancelResponse response = tossPaymentClient.cancel(
                command.paymentKey(),
                cancelReason
        );

        if (!"CANCELED".equals(response.status())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

        cancelPaidOrderTransactionService.completeCancel(command.orderId());
    }
}
