package com.app.obsession.product.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.product.application.CreateProductService;
import com.app.obsession.product.application.result.RegisterProductResult;
import com.app.obsession.product.presentation.dto.CreateProductRequest;
import com.app.obsession.product.presentation.dto.RegisterProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    private final CreateProductService createProductService;

    @PostMapping("/register")
    public CommonResponse<RegisterProductResponse> register(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RegisterProductResult result = createProductService.create(
                request.toCommand(userDetails.getMemberId())
        );

        return CommonResponse.created(
                "상품 등록에 성공했습니다.",
                RegisterProductResponse.from(result)
        );
    }

    // TODO: 상품 목록 조회
    // TODO: 상품 상세 조회
    // TODO: 상품 수정
    // TODO: 상품 삭제
}
