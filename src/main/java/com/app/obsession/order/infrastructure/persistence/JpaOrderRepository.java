package com.app.obsession.order.infrastructure.persistence;

import com.app.obsession.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {

}
