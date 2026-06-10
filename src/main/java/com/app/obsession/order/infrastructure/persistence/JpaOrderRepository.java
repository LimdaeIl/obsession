package com.app.obsession.order.infrastructure.persistence;

import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    List<Order> findTop50ByStatusAndCreatedAtBeforeOrderByIdAsc(
            OrderStatus status,
            LocalDateTime createdAt
    );
}
