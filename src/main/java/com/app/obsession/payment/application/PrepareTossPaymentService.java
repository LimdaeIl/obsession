package com.app.obsession.payment.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.application.port.PaymentStatusHistoryRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.domain.PaymentStatus;
import com.app.obsession.payment.domain.PaymentStatusHistory;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import com.app.obsession.payment.presentation.dto.TossPaymentPrepareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PrepareTossPaymentService {

    private final OrderRepository orderRepository;
    private final TossOrderIdGenerator tossOrderIdGenerator;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Transactional
    public TossPaymentPrepareResponse prepare(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!order.isPayableBy(memberId)) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE_ORDER);
        }

        return paymentRepository.findByOrderId(order.getId())
                .map(payment -> prepareExistingPayment(order, payment))
                .orElseGet(() -> prepareNewPayment(order));
    }

    private TossPaymentPrepareResponse prepareExistingPayment(Order order, Payment payment) {
        if (payment.isReady()) {
            return new TossPaymentPrepareResponse(
                    payment.getTossOrderId(),
                    payment.getAmount().longValue(),
                    "OBSESSION 주문-" + order.getId()
            );
        }

        throw new PaymentException(PaymentErrorCode.DUPLICATE_PAYMENT);
    }

    private TossPaymentPrepareResponse prepareNewPayment(Order order) {
        String tossOrderId = tossOrderIdGenerator.generate(order.getId());

        Payment payment = Payment.ready(
                order.getId(),
                tossOrderId,
                order.getTotalAmount()
        );

        Payment savedPayment = paymentRepository.save(payment);

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(
                        savedPayment.getId(),
                        savedPayment.getOrderId(),
                        null,
                        PaymentStatus.READY,
                        "PAYMENT_READY"
                )
        );

        return new TossPaymentPrepareResponse(
                tossOrderId,
                order.getTotalAmount().longValue(),
                "OBSESSION 주문-" + order.getId()
        );
    }
}
