package com.app.obsession.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExpiredOrderReleaseScheduler {

    private final ExpiredOrderReleaseService expiredOrderReleaseService;

    @Scheduled(fixedDelay = 60_000)
    public void releaseExpiredOrders() {
        expiredOrderReleaseService.releaseExpiredOrders();
    }
}
