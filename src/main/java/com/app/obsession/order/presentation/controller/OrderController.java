package com.app.obsession.order.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.response.PageResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.order.application.CreateOrderService;
import com.app.obsession.order.application.GetOrderDetailService;
import com.app.obsession.order.application.GetOrderListService;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.presentation.dto.CreateOrderRequest;
import com.app.obsession.order.presentation.dto.CreateOrderResponse;
import com.app.obsession.order.presentation.dto.OrderDetailResponse;
import com.app.obsession.order.presentation.dto.OrderListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final CreateOrderService createOrderService;
    private final GetOrderListService getOrderListService;
    private final GetOrderDetailService getOrderDetailService;

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

    @GetMapping
    public CommonResponse<PageResponse<OrderListResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        var orders = getOrderListService.getMyOrders(
                userDetails.getMemberId(),
                pageable
        );

        return CommonResponse.success(
                "주문 목록 조회에 성공했습니다.",
                PageResponse.from(orders.map(OrderListResponse::from))
        );
    }

    @GetMapping("/{orderId}")
    public CommonResponse<OrderDetailResponse> getMyOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Order order = getOrderDetailService.getMyOrder(
                orderId,
                userDetails.getMemberId()
        );

        return CommonResponse.success(
                "주문 상세 조회에 성공했습니다.",
                OrderDetailResponse.from(order)
        );
    }
}
