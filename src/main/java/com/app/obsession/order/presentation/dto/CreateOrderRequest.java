package com.app.obsession.order.presentation.dto;

import com.app.obsession.order.application.command.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(

        @NotEmpty
        List<@Valid OrderLineRequest> orderLines

) {

    public CreateOrderCommand toCommand(Long memberId) {
        return new CreateOrderCommand(
                memberId,
                orderLines.stream()
                        .map(line -> new CreateOrderCommand.OrderLineCommand(
                                line.productId(),
                                line.quantity()
                        ))
                        .toList()
        );
    }

    public record OrderLineRequest(
            @NotNull
            Long productId,

            @NotNull
            Integer quantity
    ) {
    }
}
