package com.example.delivery.payment.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 어떠한 order에 대해 얼마를 어떠한 수단으로 결제했는지,
 * 그리고 그 결과가 성공/실패인지 DB기록하는 엔티티
 */
@Entity
@Getter
@Table(name="payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="payment_id")
    private Long paymentId;

    /**
     * order와 payment의 관계
     * 하나의 주문에 여러개의 결제(성공/실퍠)가 존재할 수 있으므로
     * Order 1 : N Payment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;

    // 수단 : CARD만 현재 결제 허용
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * 결제 : CONFIRM / FAIL
     *
     *  PostgreSQL Partial Unique Index 적용 예정
     *  - 인덱스명: uq_payments_confirm_once
     *  - 동일 주문(order_id)의 CONFIRM은 최대 1건만 허용
     *  - FAIL은 여러 건 저장 가능
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", nullable = false, length = 20)
    private PaymentState paymentState;

    public Payment(
            Order order,
            PaymentMethod paymentMethod,
            PaymentState paymentState
    ) {
        this.order = order;
        this.paymentAmount = order.getTotalAmount();
        this.paymentMethod = paymentMethod;
        this.paymentState = paymentState;
    }


}
