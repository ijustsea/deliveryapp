package com.example.delivery.order.dto;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderState;
import lombok.Getter;

/**
 * OrderResponseDto	    CUSTOMER / OWNER	주문 생성·조회 결과 반환
 *
 * 주문 생성 및 조회 시 필요한 정보를 반환하는 응답 DTO.
 * Order Entity를 직접 노출하지 않고 필요한 필드만 전달.
 */
@Getter
public class OrderResponseDto {

    private final Long orderId;
    private final Long menuId;
    private final String food;
    private final Integer counts;
    private final Long totalAmount;
    private final String deliveryAddress;
    private final OrderState orderState;

    public OrderResponseDto(Order order) {
        this.orderId = order.getOrderId();
        this.menuId = order.getMenu().getMenuId();
        this.food = order.getMenu().getFood();
        this.counts = order.getCounts();
        this.totalAmount = order.getTotalAmount();
        this.deliveryAddress = order.getDeliveryAddress();
        this.orderState = order.getOrderState();
    }
}