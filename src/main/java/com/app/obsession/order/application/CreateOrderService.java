package com.app.obsession.order.application;

import com.app.obsession.global.idempotency.IdempotencyService;
import com.app.obsession.global.lock.DistributedLockExecutor;
import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.order.application.command.CreateOrderCommand;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CreateOrderService {

    private static final Duration STOCK_LOCK_WAIT_TIME = Duration.ofSeconds(3);
    private static final Duration STOCK_LOCK_LEASE_TIME = Duration.ofSeconds(5);

    private final DistributedLockExecutor distributedLockExecutor;
    private final RedisKey redisKey;
    private final CreateOrderProcessor createOrderProcessor;
    private final IdempotencyService idempotencyService;

    public Long create(CreateOrderCommand command) {
        validate(command);

        List<Long> productIds = command.orderLines()
                .stream()
                .map(CreateOrderCommand.OrderLineCommand::productId)
                .distinct()
                .sorted()
                .toList();

        return createWithLocks(productIds, command);
    }

    public Long create(String idempotencyKey, CreateOrderCommand command) {
        return idempotencyService.execute(
                idempotencyKey,
                command,
                Long.class,
                () -> create(command)
        );
    }

    private void validate(CreateOrderCommand command) {
        if (command.memberId() == null || command.memberId() <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_MEMBER_ID);
        }

        if (command.orderLines() == null || command.orderLines().isEmpty()) {
            throw new OrderException(OrderErrorCode.EMPTY_ORDER_LINES);
        }
    }

    private Long createWithLocks(List<Long> productIds, CreateOrderCommand command) {
        if (productIds.isEmpty()) {
            throw new OrderException(OrderErrorCode.EMPTY_ORDER_LINES);
        }

        return executeLocksRecursively(productIds, 0, command);
    }

    private Long executeLocksRecursively(
            List<Long> productIds,
            int index,
            CreateOrderCommand command
    ) {
        if (index == productIds.size()) {
            return createOrderProcessor.create(command);
        }

        Long productId = productIds.get(index);

        return distributedLockExecutor.execute(
                redisKey.productStockLock(productId),
                STOCK_LOCK_WAIT_TIME,
                STOCK_LOCK_LEASE_TIME,
                () -> executeLocksRecursively(productIds, index + 1, command)
        );
    }
}

