package com.example.delivery.order.controller;

import com.example.delivery.global.security.UserDetailsImpl;
import com.example.delivery.order.dto.OrderCreateRequestDto;
import com.example.delivery.order.dto.OrderResponseDto;
import com.example.delivery.order.dto.OrderStateUpdateRequestDto;
import com.example.delivery.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController
 *
 * 주문 관련 HTTP 요청을 수신하는 Controller.
 * 인증된 사용자 ID와 요청 DTO를 Service에 전달하고,
 * 처리 결과를 HTTP 상태 코드와 응답 DTO로 반환.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // HTTP 요청 처리 메서드 4개
    // 1.CUSTOMER 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long currentUserId = userDetails.getUser().getUserId();
        OrderResponseDto response = orderService.createOrder(request, currentUserId);

        // HTTP 201 Created + OrderResponseDto를 JSON Body로 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 2.CUSTOMER/OWNER별 주문 목록 조회
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUser().getUserId();

        List<OrderResponseDto> response =
                orderService.getOrders(currentUserId);

        return ResponseEntity.ok(response);
    }
    // 3.CUSTOMER 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUser().getUserId();

        orderService.cancelOrder(orderId, currentUserId);

        return ResponseEntity.noContent().build();
    }
    // 4.OWNER 본인 메뉴의 주문 상태 변경
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderState(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStateUpdateRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUser().getUserId();

        orderService.updateOrderState(orderId, currentUserId, request);

        return ResponseEntity.noContent().build();
    }
}
