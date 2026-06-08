package com.app.obsession.order.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.order.application.CreateOrderService;
import com.app.obsession.order.presentation.dto.CreateOrderRequest;
import com.app.obsession.order.presentation.dto.CreateOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final CreateOrderService createOrderService;

    @PostMapping
    public CommonResponse<CreateOrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long orderId = createOrderService.create(
                request.toCommand(userDetails.getMemberId())
        );

        return CommonResponse.created(
                "주문 생성에 성공했습니다.",
                new CreateOrderResponse(orderId)
        );
    }
}
