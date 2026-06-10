package com.app.obsession.order.application.port;

import com.app.obsession.order.domain.OrderStatusHistory;

public interface OrderStatusHistoryRepository {

    OrderStatusHistory save(OrderStatusHistory history);
}
