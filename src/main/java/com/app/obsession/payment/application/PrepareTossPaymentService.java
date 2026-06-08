package com.app.obsession.payment.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
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

    @Transactional(readOnly = true)
    public TossPaymentPrepareResponse prepare(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!order.isPayableBy(memberId)) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE_ORDER);
        }

        String tossOrderId = tossOrderIdGenerator.generate(order.getId());

        return new TossPaymentPrepareResponse(
                tossOrderId,
                order.getTotalAmount().longValue(),
                "OBSESSION 주문-" + order.getId()
        );
    }
}
