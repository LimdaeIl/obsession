package com.app.obsession.order.application.port;

import com.app.obsession.order.domain.Order;

public interface OrderRepository {

    Order save(Order order);
}
