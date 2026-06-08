package com.app.obsession.payment.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
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

        return paymentRepository.findByOrderId(orderId)
                .map(Payment::getId)
                .orElseGet(() -> confirmNewPayment(
                        memberId,
                        paymentKey,
                        tossOrderId,
                        amount,
                        orderId
                ));
    }

    private Long confirmNewPayment(
            Long memberId,
            String paymentKey,
            String tossOrderId,
            Long amount,
            Long orderId
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!order.isPayableBy(memberId)) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE_ORDER);
        }

        if (order.getTotalAmount().compareTo(BigDecimal.valueOf(amount)) != 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        TossPaymentResponse response = tossPaymentClient.confirm(
                paymentKey,
                tossOrderId,
                amount
        );

        if (!"DONE".equals(response.status())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_APPROVED);
        }

        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new PaymentException(PaymentErrorCode.PRODUCT_STOCK_NOT_FOUND));

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
            throw new PaymentException(PaymentErrorCode.INVALID_ORDER_NUMBER);
        }

        String[] parts = tossOrderId.split("-");

        if (parts.length < 2) {
            throw new PaymentException(PaymentErrorCode.INVALID_ORDER_NUMBER);
        }

        try {
            return Long.valueOf(parts[1]);
        } catch (NumberFormatException e) {
            throw new PaymentException(PaymentErrorCode.INVALID_ORDER_NUMBER);
        }
    }
}
