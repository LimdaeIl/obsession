package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExpiredOrderReleaseService {

    private static final long ORDER_PAYMENT_TIMEOUT_MINUTES = 30;

    private final OrderRepository orderRepository;
    private final ExpiredOrderReleaseProcessor expiredOrderReleaseProcessor;
    private final Clock clock;

    public void releaseExpiredOrders() {
        LocalDateTime expiredBefore = LocalDateTime.now(clock)
                .minusMinutes(ORDER_PAYMENT_TIMEOUT_MINUTES);

        List<Order> orders = orderRepository.findExpiredCreatedOrders(expiredBefore);

        for (Order order : orders) {
            try {
                expiredOrderReleaseProcessor.expire(order.getId());
            } catch (RuntimeException e) {
                log.warn("Failed to release expired order. orderId={}", order.getId(), e);
            }
        }
    }
}
