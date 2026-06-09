package com.app.obsession.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.obsession.order.application.command.CreateOrderCommand;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import com.app.obsession.product.domain.ProductStock;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CreateOrderServiceConcurrencyTest {

    @Autowired
    private CreateOrderService createOrderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Test
    @DisplayName("[AFTER] Redis Lock 적용 후 동시 주문 시 재고 초과 예약을 방지한다")
    void createOrder_concurrently_afterRedisLock() throws Exception {
        int initialStock = 10;
        int requestCount = 100;
        int threadCount = 32;

        Product product = productRepository.save(
                Product.create(
                        1L,
                        "Redis Lock 테스트 상품",
                        "동시성 테스트 상품",
                        BigDecimal.valueOf(10_000),
                        ProductStatus.ON_SALE
                )
        );

        productStockRepository.save(
                ProductStock.create(product.getId(), initialStock)
        );

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        var executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < requestCount; i++) {
            long memberId = i + 1L;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    createOrderService.create(
                            new CreateOrderCommand(
                                    memberId,
                                    List.of(
                                            new CreateOrderCommand.OrderLineCommand(
                                                    product.getId(),
                                                    1
                                            )
                                    )
                            )
                    );

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println(
                            "fail: " + e.getClass().getSimpleName()
                                    + " - " + e.getMessage()
                    );
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(finished).isTrue();

        ProductStock resultStock = productStockRepository.findByProductId(product.getId())
                .orElseThrow();

        System.out.println("===== Redis Lock 동시성 테스트 결과 =====");
        System.out.println("initialStock = " + initialStock);
        System.out.println("requestCount = " + requestCount);
        System.out.println("successCount = " + successCount.get());
        System.out.println("failCount = " + failCount.get());
        System.out.println("totalQuantity = " + resultStock.getTotalQuantity());
        System.out.println("reservedQuantity = " + resultStock.getReservedQuantity());
        System.out.println("soldQuantity = " + resultStock.getSoldQuantity());
        System.out.println("availableQuantity = " + resultStock.availableQuantity());

        assertThat(successCount.get()).isEqualTo(initialStock);
        assertThat(failCount.get()).isEqualTo(requestCount - initialStock);
        assertThat(resultStock.getReservedQuantity()).isEqualTo(initialStock);
        assertThat(resultStock.getSoldQuantity()).isZero();
        assertThat(resultStock.availableQuantity()).isZero();
    }
}
