package com.app.obsession.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.lock.DistributedLockExecutor;
import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.order.application.command.CreateOrderCommand;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CreateOrderServiceTest {

    @Mock
    private DistributedLockExecutor distributedLockExecutor;

    @Mock
    private RedisKey redisKey;

    @Mock
    private CreateOrderProcessor createOrderProcessor;

    private CreateOrderService createOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createOrderService = new CreateOrderService(
                distributedLockExecutor,
                redisKey,
                createOrderProcessor
        );
    }

    @Test
    @DisplayName("주문 생성 시 productId 기준 Redis Lock을 획득한 뒤 Processor를 호출한다")
    void create_success_withRedisLock() {
        Long memberId = 1L;
        Long productId = 10L;
        Long orderId = 100L;

        CreateOrderCommand command = new CreateOrderCommand(
                memberId,
                List.of(new CreateOrderCommand.OrderLineCommand(productId, 1))
        );

        when(redisKey.productStockLock(productId)).thenReturn("obsession:local:lock:product-stock:10");

        when(distributedLockExecutor.execute(
                eq("obsession:local:lock:product-stock:10"),
                eq(Duration.ofSeconds(3)),
                eq(Duration.ofSeconds(5)),
                any()
        )).thenAnswer(invocation -> {
            Supplier<Long> supplier = invocation.getArgument(3);
            return supplier.get();
        });

        when(createOrderProcessor.create(command)).thenReturn(orderId);

        Long result = createOrderService.create(command);

        assertThat(result).isEqualTo(orderId);

        verify(distributedLockExecutor).execute(
                eq("obsession:local:lock:product-stock:10"),
                eq(Duration.ofSeconds(3)),
                eq(Duration.ofSeconds(5)),
                any()
        );

        verify(createOrderProcessor).create(command);
    }

    @Test
    @DisplayName("여러 상품 주문 시 productId 오름차순으로 Lock을 획득한다")
    void create_locksInSortedProductIdOrder() {
        Long memberId = 1L;

        CreateOrderCommand command = new CreateOrderCommand(
                memberId,
                List.of(
                        new CreateOrderCommand.OrderLineCommand(30L, 1),
                        new CreateOrderCommand.OrderLineCommand(10L, 1),
                        new CreateOrderCommand.OrderLineCommand(20L, 1)
                )
        );

        when(redisKey.productStockLock(10L)).thenReturn("lock:10");
        when(redisKey.productStockLock(20L)).thenReturn("lock:20");
        when(redisKey.productStockLock(30L)).thenReturn("lock:30");

        when(distributedLockExecutor.execute(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    Supplier<Long> supplier = invocation.getArgument(3);
                    return supplier.get();
                });

        when(createOrderProcessor.create(command)).thenReturn(1L);

        createOrderService.create(command);

        ArgumentCaptor<String> lockKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(distributedLockExecutor, org.mockito.Mockito.times(3)).execute(
                lockKeyCaptor.capture(),
                eq(Duration.ofSeconds(3)),
                eq(Duration.ofSeconds(5)),
                any()
        );

        assertThat(lockKeyCaptor.getAllValues())
                .containsExactly("lock:10", "lock:20", "lock:30");
    }
}
