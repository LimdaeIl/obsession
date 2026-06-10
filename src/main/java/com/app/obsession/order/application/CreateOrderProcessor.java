package com.app.obsession.order.application;

import com.app.obsession.order.application.command.CreateOrderCommand;
import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.domain.OrderStatusHistory;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrderProcessor {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private static final String ORDER_CREATED_REASON = "ORDER_CREATED";

    @Transactional
    public Long create(CreateOrderCommand command) {
        Order order = Order.create(command.memberId());

        Map<Long, Integer> orderLineQuantities = mergeOrderLines(command);

        for (Map.Entry<Long, Integer> entry : orderLineQuantities.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

            if (!product.getStatus().canSell()) {
                throw new ProductException(ProductErrorCode.NOT_ON_SALE_PRODUCT);
            }

            ProductStock stock = productStockRepository.findByProductId(product.getId())
                    .orElseThrow(
                            () -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.reserve(quantity);

            order.addOrderLine(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity
            );
        }

        order.validateCreatable();

        Order savedOrder = orderRepository.save(order);

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(
                        savedOrder.getId(),
                        null,
                        OrderStatus.CREATED,
                        ORDER_CREATED_REASON
                )
        );

        return savedOrder.getId();
    }

    private Map<Long, Integer> mergeOrderLines(CreateOrderCommand command) {
        return command.orderLines()
                .stream()
                .collect(Collectors.toMap(
                        CreateOrderCommand.OrderLineCommand::productId,
                        CreateOrderCommand.OrderLineCommand::quantity,
                        Integer::sum
                ));
    }
}
