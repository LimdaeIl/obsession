package com.app.obsession.payment.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.infrastructure.external.TossPaymentClient;
import com.app.obsession.payment.infrastructure.external.TossPaymentResponse;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ConfirmTossPaymentService {

    private final OrderRepository orderRepository;
    private final ProductStockRepository productStockRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public Long confirm(
            Long memberId,
            String paymentKey,
            String tossOrderId,
            Long amount
    ) {
        Long orderId = parseOrderId(tossOrderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.isPayableBy(memberId)) {
            throw new IllegalStateException("결제할 수 없는 주문입니다.");
        }

        if (order.getTotalAmount().compareTo(BigDecimal.valueOf(amount)) != 0) {
            throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        TossPaymentResponse response = tossPaymentClient.confirm(
                paymentKey,
                tossOrderId,
                amount
        );

        if (!"DONE".equals(response.status())) {
            throw new IllegalStateException("결제가 정상 승인되지 않았습니다.");
        }

        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new IllegalStateException("상품 재고 정보를 찾을 수 없습니다."));

            stock.confirm(orderLine.getQuantity());
        }

        order.markPaid();

        Payment payment = Payment.paid(
                order.getId(),
                response.orderId(),
                response.paymentKey(),
                BigDecimal.valueOf(response.totalAmount()),
                response.method()
        );

        Payment savedPayment = paymentRepository.save(payment);

        return savedPayment.getId();
    }

    private Long parseOrderId(String tossOrderId) {
        if (tossOrderId == null || !tossOrderId.startsWith("ORDER-")) {
            throw new IllegalArgumentException("주문번호 형식이 올바르지 않습니다.");
        }

        return Long.valueOf(tossOrderId.replace("ORDER-", ""));
    }
}
