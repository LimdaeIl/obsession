package com.app.obsession.order.infrastructure.persistence;

import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.OrderStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderStatusHistoryRepositoryImpl implements OrderStatusHistoryRepository {

    private final JpaOrderStatusHistoryRepository jpaOrderStatusHistoryRepository;

    @Override
    public OrderStatusHistory save(OrderStatusHistory history) {
        return jpaOrderStatusHistoryRepository.save(history);
    }
}
