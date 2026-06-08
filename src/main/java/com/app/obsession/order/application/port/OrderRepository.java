package com.app.obsession.order.application.port;

import com.app.obsession.order.domain.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Order save(Order order);

    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    Optional<Order> findById(Long orderId);
}
