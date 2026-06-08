package com.app.obsession.product.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.response.PageResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.product.application.CreateProductService;
import com.app.obsession.product.application.DeleteProductService;
import com.app.obsession.product.application.GetProductDetailService;
import com.app.obsession.product.application.GetProductListService;
import com.app.obsession.product.application.UpdateProductService;
import com.app.obsession.product.application.command.DeleteProductCommand;
import com.app.obsession.product.application.result.RegisterProductResult;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductActor;
import com.app.obsession.product.presentation.dto.CreateProductRequest;
import com.app.obsession.product.presentation.dto.ProductDetailResponse;
import com.app.obsession.product.presentation.dto.ProductListResponse;
import com.app.obsession.product.presentation.dto.ProductSearchRequest;
import com.app.obsession.product.presentation.dto.RegisterProductResponse;
import com.app.obsession.product.presentation.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    private final CreateProductService createProductService;
    private final GetProductListService getProductListService;
    private final GetProductDetailService getProductDetailService;
    private final UpdateProductService updateProductService;
    private final DeleteProductService deleteProductService;

    @PostMapping("/register")
    public CommonResponse<RegisterProductResponse> register(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RegisterProductResult result = createProductService.create(
                request.toCommand(
                        userDetails.getMemberId(),
                        userDetails.getRole()
                ));

        return CommonResponse.created(
                "상품 등록에 성공했습니다.",
                RegisterProductResponse.from(result)
        );
    }


    @GetMapping
    public CommonResponse<PageResponse<ProductListResponse>> getProducts(
            @ModelAttribute ProductSearchRequest request
    ) {
        Page<Product> products = getProductListService.getProducts(request.keyword(),
                request.status(), request.toPageable());

        return CommonResponse.success(
                "상품 목록 조회에 성공했습니다.",
                PageResponse.from(products.map(ProductListResponse::from))
        );
    }

    @GetMapping("/{productId}")
    public CommonResponse<ProductDetailResponse> getProduct(
            @PathVariable Long productId
    ) {
        Product product = getProductDetailService.getProduct(productId);

        return CommonResponse.success(
                "상품 상세 조회에 성공했습니다.",
                ProductDetailResponse.from(product)
        );
    }

    // 추후 고도화 필요한 부분이다.
    //    PUT    /products/{id}             상품 정보 수정
    //    PATCH  /products/{id}/status      상태 변경
    //    PUT    /products/{id}/images      이미지 변경
    //    PATCH  /products/{id}/stock       재고 수정
    //    DELETE /products/{id}             상품 삭제
    @PutMapping("/{productId}")
    public CommonResponse<Void> update(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        updateProductService.update(
                request.toCommand(
                        productId,
                        userDetails.getMemberId(),
                        userDetails.getRole()
                )
        );

        return CommonResponse.success("상품 수정에 성공했습니다.");
    }

    // TODO: 상품 삭제
    @DeleteMapping("/{productId}")
    public CommonResponse<Void> delete(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        deleteProductService.delete(
                new DeleteProductCommand(
                        productId,
                        new ProductActor(
                                userDetails.getMemberId(),
                                userDetails.getRole()
                        )
                )
        );

        return CommonResponse.success("상품 삭제에 성공했습니다.");
    }

}
