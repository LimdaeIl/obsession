package com.app.obsession.product.presentation.dto;

import com.app.obsession.product.application.command.CreateProductCommand;
import com.app.obsession.product.domain.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(

        @NotBlank(message = "상품 등록: 상품명은 필수입니다.")
        @Size(max = 100, message = "상품 등록: 상품명은 최대 100자까지 입력할 수 있습니다.")
        String name,

        @NotBlank(message = "상품 등록: 상품 설명은 필수입니다.")
        @Size(max = 1000, message = "상품 등록: 상품 설명은 최대 1000자까지 입력할 수 있습니다.")
        String description,

        @NotNull(message = "상품 등록: 가격은 필수입니다.")
        @DecimalMin(value = "0.01", message = "상품 등록: 가격은 0보다 커야 합니다.")
        BigDecimal price,

        @NotNull(message = "상품 등록: 상품 상태는 필수입니다.")
        ProductStatus status,

        @Min(value = 0, message = "상품 등록: 초기 재고는 0 이상이어야 합니다.")
        int initialStock,

        @Size(max = 10, message = "상품 등록: 상품 이미지는 최대 10개까지 등록할 수 있습니다.")
        List<
                @NotBlank(message = "상품 등록: 이미지 URL은 비어 있을 수 없습니다.")
                @Size(max = 500, message = "상품 등록: 이미지 URL은 최대 500자까지 입력할 수 있습니다.")
                        String
                > imageUrls
) {
    public CreateProductCommand toCommand(Long memberId) {
        return new CreateProductCommand(
                memberId,
                name,
                description,
                price,
                status,
                initialStock,
                imageUrls
        );
    }
}
