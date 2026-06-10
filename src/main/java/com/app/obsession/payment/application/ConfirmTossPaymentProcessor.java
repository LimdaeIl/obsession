package com.app.obsession.payment.application;

import com.app.obsession.global.outbox.OutboxEventService;
import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.domain.OrderStatusHistory;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.application.port.PaymentStatusHistoryRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.domain.PaymentStatus;
import com.app.obsession.payment.domain.PaymentStatusHistory;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import com.app.obsession.payment.infrastructure.external.TossPaymentClient;
import com.app.obsession.payment.infrastructure.external.TossPaymentResponse;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfirmTossPaymentProcessor {

    private final OrderRepository orderRepository;
    private final ProductStockRepository productStockRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final OutboxEventService outboxEventService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Transactional
    public Long confirm(
            Long memberId,
            String paymentKey,
            String tossOrderId,
            Long amount
    ) {
        validatePaymentKey(paymentKey);

        Payment payment = paymentRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isApproved()) {
            return payment.getId();
        }

        if (!payment.isReady()) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE_ORDER);
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        validateDuplicatePaymentKey(paymentKey, payment);
        validate(memberId, amount, payment, order);

        TossPaymentResponse response;

        try {
            response = tossPaymentClient.confirm(
                    paymentKey,
                    tossOrderId,
                    amount
            );
        } catch (PaymentException e) {
            failPayment(order, payment);
            throw e;
        }

        if (!"DONE".equals(response.status())) {
            failPayment(order, payment);
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_APPROVED);
        }

        confirmStocks(order);
        OrderStatus fromStatus = order.getStatus();
        order.markPaid();

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(
                        order.getId(),
                        fromStatus,
                        OrderStatus.PAID,
                        "PAYMENT_APPROVED"
                )
        );

        PaymentStatus paymentFromStatus = payment.getStatus();

        payment.approve(
                response.paymentKey(),
                response.method()
        );

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(
                        payment.getId(),
                        payment.getOrderId(),
                        paymentFromStatus,
                        PaymentStatus.APPROVED,
                        "PAYMENT_APPROVED"
                )
        );

        return payment.getId();
    }

    private void validate(
            Long memberId,
            Long amount,
            Payment payment,
            Order order
    ) {
        if (!order.isPayableBy(memberId)) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE_ORDER);
        }

        BigDecimal requestAmount = BigDecimal.valueOf(amount);

        if (order.getTotalAmount().compareTo(requestAmount) != 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        if (payment.getAmount().compareTo(requestAmount) != 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private void confirmStocks(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(
                            () -> new PaymentException(PaymentErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.confirm(orderLine.getQuantity());
        }
    }

    private void releaseStocks(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(
                            () -> new PaymentException(PaymentErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.release(orderLine.getQuantity());
        }
    }

    private void failPayment(Order order, Payment payment) {
        OrderStatus fromStatus = order.getStatus();
        order.markFailed();

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(
                        order.getId(),
                        fromStatus,
                        OrderStatus.FAILED,
                        "PAYMENT_FAILED"
                )
        );

        PaymentStatus paymentFromStatus = payment.getStatus();
        payment.fail();

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(
                        payment.getId(),
                        payment.getOrderId(),
                        paymentFromStatus,
                        PaymentStatus.FAILED,
                        "PAYMENT_FAILED"
                )
        );

        try {
            releaseStocks(order);
        } catch (RuntimeException e) {
            outboxEventService.savePending(
                    "STOCK_RELEASE_FAILED",
                    """
                            {
                              "orderId": %d,
                              "paymentId": %d
                            }
                            """.formatted(order.getId(), payment.getId())
            );

            log.warn(
                    "Failed to release stocks for orderId={}, paymentId={}",
                    order.getId(),
                    payment.getId(),
                    e
            );
        }
    }


    private void validateDuplicatePaymentKey(String paymentKey, Payment currentPayment) {
        paymentRepository.findByPaymentKey(paymentKey)
                .ifPresent(existingPayment -> {
                    if (!existingPayment.getId().equals(currentPayment.getId())) {
                        throw new PaymentException(PaymentErrorCode.DUPLICATE_PAYMENT);
                    }
                });
    }

    private void validatePaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_KEY);
        }
    }
}
