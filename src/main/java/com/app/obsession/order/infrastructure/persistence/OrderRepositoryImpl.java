package com.app.obsession.order.infrastructure.persistence;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }

    @Override
    public Page<Order> findByMemberId(Long memberId, Pageable pageable) {
        return jpaOrderRepository.findByMemberId(memberId, pageable);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return jpaOrderRepository.findById(orderId);
    }

    @Override
    public List<Order> findExpiredCreatedOrders(LocalDateTime expiredBefore) {
        return jpaOrderRepository.findTop50ByStatusAndCreatedAtBeforeOrderByIdAsc(
                OrderStatus.CREATED,
                expiredBefore
        );
    }
}
