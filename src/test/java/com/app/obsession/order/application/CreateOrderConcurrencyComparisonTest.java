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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CreateOrderConcurrencyComparisonTest {

    private static final int INITIAL_STOCK = 10;
    private static final int REQUEST_COUNT = 100;
    private static final int THREAD_COUNT = 32;

    @Autowired
    private TestCreateOrderServiceV1 createOrderServiceV1;

    @Autowired
    private CreateOrderService createOrderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Test
    @DisplayName("[BEFORE] Optimistic Lock 기반 동시 주문 결과를 기록한다")
    void before_optimisticLock() throws Exception {
        Product product = createProductAndStock("BEFORE");

        ConcurrencyTestResult result = runConcurrencyTest(
                product.getId(),
                memberId -> createOrderServiceV1.create(
                        new CreateOrderCommand(
                                memberId,
                                List.of(new CreateOrderCommand.OrderLineCommand(product.getId(), 1))
                        )
                )
        );

        ProductStock resultStock = productStockRepository.findByProductId(product.getId())
                .orElseThrow();

        printResult("BEFORE Optimistic Lock", result, resultStock);

        assertThat(result.successCount()).isEqualTo(INITIAL_STOCK);
        assertThat(result.failCount()).isEqualTo(REQUEST_COUNT - INITIAL_STOCK);
        assertThat(resultStock.getReservedQuantity()).isEqualTo(INITIAL_STOCK);
        assertThat(resultStock.availableQuantity()).isZero();

        assertThat(result.containsOptimisticLockException())
                .as("BEFORE에서는 Optimistic Lock 충돌이 발생하는지 기록한다")
                .isTrue();
    }

    @Test
    @DisplayName("[AFTER] Redis Distributed Lock 기반 동시 주문 결과를 검증한다")
    void after_redisDistributedLock() throws Exception {
        Product product = createProductAndStock("AFTER");

        ConcurrencyTestResult result = runConcurrencyTest(
                product.getId(),
                memberId -> createOrderService.create(
                        new CreateOrderCommand(
                                memberId,
                                List.of(new CreateOrderCommand.OrderLineCommand(product.getId(), 1))
                        )
                )
        );

        ProductStock resultStock = productStockRepository.findByProductId(product.getId())
                .orElseThrow();

        printResult("AFTER Redis Distributed Lock", result, resultStock);

        assertThat(result.successCount()).isEqualTo(INITIAL_STOCK);
        assertThat(result.failCount()).isEqualTo(REQUEST_COUNT - INITIAL_STOCK);
        assertThat(resultStock.getReservedQuantity()).isEqualTo(INITIAL_STOCK);
        assertThat(resultStock.getSoldQuantity()).isZero();
        assertThat(resultStock.availableQuantity()).isZero();

        assertThat(result.containsOptimisticLockException())
                .as("AFTER에서는 Redis Lock으로 임계 구역을 직렬화하므로 Optimistic Lock 충돌이 없어야 한다")
                .isFalse();

        assertThat(result.exceptionCounts())
                .as("AFTER 실패는 비즈니스 재고 부족 예외로 수렴해야 한다")
                .containsKey("ProductException");
    }

    private Product createProductAndStock(String prefix) {
        Product product = productRepository.save(
                Product.create(
                        1L,
                        prefix + " 동시성 테스트 상품-" + UUID.randomUUID(),
                        prefix + " 재고 동시성 테스트 상품",
                        BigDecimal.valueOf(10_000),
                        ProductStatus.ON_SALE
                )
        );

        productStockRepository.save(
                ProductStock.create(product.getId(), INITIAL_STOCK)
        );

        return product;
    }

    private ConcurrencyTestResult runConcurrencyTest(
            Long productId,
            Function<Long, Long> orderCreator
    ) throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(REQUEST_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ConcurrentHashMap<String, AtomicInteger> exceptionCounts = new ConcurrentHashMap<>();

        var executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < REQUEST_COUNT; i++) {
            long memberId = i + 1L;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    orderCreator.apply(memberId);

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    collectException(exceptionCounts, e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(finished)
                .as("동시성 테스트가 제한 시간 내 종료되어야 한다. productId=" + productId)
                .isTrue();

        return new ConcurrencyTestResult(
                successCount.get(),
                failCount.get(),
                exceptionCounts
        );
    }

    private void collectException(
            ConcurrentHashMap<String, AtomicInteger> exceptionCounts,
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current != null) {
            exceptionCounts
                    .computeIfAbsent(current.getClass().getSimpleName(), key -> new AtomicInteger())
                    .incrementAndGet();

            current = current.getCause();
        }
    }

    private void printResult(
            String title,
            ConcurrencyTestResult result,
            ProductStock stock
    ) {
        System.out.println("===== " + title + " 동시성 테스트 결과 =====");
        System.out.println("initialStock = " + INITIAL_STOCK);
        System.out.println("requestCount = " + REQUEST_COUNT);
        System.out.println("successCount = " + result.successCount());
        System.out.println("failCount = " + result.failCount());
        System.out.println("totalQuantity = " + stock.getTotalQuantity());
        System.out.println("reservedQuantity = " + stock.getReservedQuantity());
        System.out.println("soldQuantity = " + stock.getSoldQuantity());
        System.out.println("availableQuantity = " + stock.availableQuantity());
        System.out.println("exceptionCounts = " + toPrintableMap(result.exceptionCounts()));
    }

    private Map<String, Integer> toPrintableMap(Map<String, AtomicInteger> exceptionCounts) {
        return exceptionCounts.entrySet()
                .stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().get()
                        )
                );
    }

    private record ConcurrencyTestResult(
            int successCount,
            int failCount,
            Map<String, AtomicInteger> exceptionCounts
    ) {

        private boolean containsOptimisticLockException() {
            return exceptionCounts.containsKey("ObjectOptimisticLockingFailureException")
                    || exceptionCounts.containsKey("OptimisticLockException")
                    || exceptionCounts.containsKey("StaleObjectStateException")
                    || exceptionCounts.containsKey("OptimisticLockingFailureException");
        }
    }
}
