package com.app.obsession.order.application.command;

import java.util.List;

public record CreateOrderCommand(
        Long memberId,
        List<OrderLineCommand> orderLines
) {

    public record OrderLineCommand(
            Long productId,
            int quantity
    ) {
    }
}
