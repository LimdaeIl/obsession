package com.app.obsession.order.application;

import com.app.obsession.order.application.command.CreateOrderCommand;
import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CreateOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public Long create(CreateOrderCommand command) {
        validate(command);

        Order order = Order.create(command.memberId());

        for (CreateOrderCommand.OrderLineCommand line : command.orderLines()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            if (!product.getStatus().canSell()) {
                throw new IllegalStateException("판매 중인 상품만 주문할 수 있습니다.");
            }

            ProductStock stock = productStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new IllegalStateException("상품 재고 정보를 찾을 수 없습니다."));

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
            throw new IllegalArgumentException("회원 ID가 올바르지 않습니다.");
        }

        if (command.orderLines() == null || command.orderLines().isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 1개 이상이어야 합니다.");
        }
    }
}
