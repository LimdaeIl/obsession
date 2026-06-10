package com.app.obsession.order.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.response.PageResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.order.application.CancelOrderService;
import com.app.obsession.order.application.CancelPaidOrderService;
import com.app.obsession.order.application.CreateOrderService;
import com.app.obsession.order.application.GetOrderDetailService;
import com.app.obsession.order.application.GetOrderListService;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.presentation.dto.CancelPaidOrderRequest;
import com.app.obsession.order.presentation.dto.CreateOrderRequest;
import com.app.obsession.order.presentation.dto.CreateOrderResponse;
import com.app.obsession.order.presentation.dto.OrderDetailResponse;
import com.app.obsession.order.presentation.dto.OrderListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final CancelOrderService cancelOrderService;
    private final CancelPaidOrderService cancelPaidOrderService;

    @PostMapping
    public CommonResponse<CreateOrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        Long orderId = createOrderService.create(
                idempotencyKey,
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
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Direction.DESC, "id")
        );
        Page<Order> orders = getOrderListService.getMyOrders(
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
        OrderDetailResponse response = getOrderDetailService.getMyOrder(
                orderId,
                userDetails.getMemberId()
        );

        return CommonResponse.success(
                "주문 상세 조회에 성공했습니다.",
                response
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public CommonResponse<Void> cancel(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cancelOrderService.cancel(
                orderId,
                userDetails.getMemberId()
        );

        return CommonResponse.success("주문 취소에 성공했습니다.");
    }

    @PostMapping("/{orderId}/cancel-paid")
    public CommonResponse<Void> cancelPaid(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelPaidOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cancelPaidOrderService.cancel(
                orderId,
                userDetails.getMemberId(),
                request.cancelReason()
        );

        return CommonResponse.success("결제 완료 주문 취소에 성공했습니다.");
    }

}
