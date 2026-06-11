package com.app.obsession.order.application;

import com.app.obsession.order.application.command.CreateOrderCommand;
import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class TestCreateOrderServiceV1 {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public Long create(CreateOrderCommand command) {
        validate(command);

        Order order = Order.create(command.memberId());

        for (CreateOrderCommand.OrderLineCommand line : command.orderLines()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

            if (product.getStatus().canSell()) {
                throw new ProductException(ProductErrorCode.NOT_ON_SALE_PRODUCT);
            }

            ProductStock stock = productStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.reserve(line.quantity());

            order.addOrderLine(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    line.quantity()
            );
        }

        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }

    private void validate(CreateOrderCommand command) {
        if (command.memberId() == null || command.memberId() <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_MEMBER_ID);
        }

        if (command.orderLines() == null || command.orderLines().isEmpty()) {
            throw new OrderException(OrderErrorCode.EMPTY_ORDER_LINES);
        }
    }
}
