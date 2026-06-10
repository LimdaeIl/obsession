package com.app.obsession.payment.application;

import com.app.obsession.global.outbox.OutboxEvent;
import com.app.obsession.global.outbox.OutboxEventProcessor;
import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class StockReleaseFailedOutboxProcessor implements OutboxEventProcessor {

    private static final String EVENT_TYPE = "STOCK_RELEASE_FAILED";

    private final OrderRepository orderRepository;
    private final ProductStockRepository productStockRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String eventType) {
        return EVENT_TYPE.equals(eventType);
    }

    @Override
    public void process(OutboxEvent event) {
        StockReleaseFailedPayload payload = deserialize(event.getPayload());

        Order order = orderRepository.findById(payload.orderId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new PaymentException(PaymentErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.release(orderLine.getQuantity());
        }
    }

    private StockReleaseFailedPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, StockReleaseFailedPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize stock release failed payload.", e);
        }
    }

    private record StockReleaseFailedPayload(
            Long orderId,
            Long paymentId
    ) {
    }
}
