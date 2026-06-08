package com.app.obsession.product.presentation.dto;

import com.app.obsession.product.application.command.UpdateProductCommand;
import com.app.obsession.product.domain.ProductActor;
import com.app.obsession.product.domain.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank
        String name,

        @NotBlank
        String description,

        @NotNull
        BigDecimal price,

        @NotNull
        ProductStatus status

) {

    public UpdateProductCommand toCommand(
            Long productId,
            Long memberId,
            String role
    ) {
        return new UpdateProductCommand(
                productId,
                new ProductActor(memberId, role),
                name,
                description,
                price,
                status
        );
    }
}