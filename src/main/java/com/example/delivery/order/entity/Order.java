package com.example.delivery.order.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // CUSTOMER 1 : N Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Menu 1 : N Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "counts", nullable = false)
    private Integer counts;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false, length = 20)
    private OrderState orderState;

    // 주문 생성 시 entity 초기화에 사용하는 생성자
    public Order(
            User user,
            Menu menu,
            Integer counts,
            Long totalAmount,
            String deliveryAddress
    ) {
        this.user = user;
        this.menu = menu;
        this.counts = counts;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.orderState = OrderState.PENDING;
    }

    //OrderState 상태변경 메소드
    // 1.주문 취소 (CUSTOMER)
    public void cancel() {
        validateState(OrderState.PENDING);
        this.orderState = OrderState.CANCELED;
    }

    // 2.결제 완료 (PaymentService)
    public void markPaid() {
        validateState(OrderState.PENDING);
        this.orderState = OrderState.PAID;
    }

    // 3.주문 수락 (OWNER)
    public void accept() {
        validateState(OrderState.PAID);
        this.orderState = OrderState.ACCEPTED;
    }

    // 4,배달 완료 (OWNER)
    public void complete() {
        validateState(OrderState.ACCEPTED);
        this.orderState = OrderState.COMPLETED;
    }

    // 주문의 상태 검사 메소드
    // 예상된 주문 상태가 아니면 예외를 발생시켜 상태 변경 중단
    private void validateState(OrderState expected) {
        if (this.orderState != expected) {
            throw new IllegalStateException(
                    //현재 500반환 -> 409 Conflict를 반환으로 수정 예정
                    "Invalid order state: " + this.orderState
            );
        }
    }
}
