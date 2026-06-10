package com.app.obsession.order.infrastructure.persistence;

import com.app.obsession.order.domain.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}
