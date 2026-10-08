package com.example.delivery.payment.dto;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentMethod;
import com.example.delivery.payment.entity.PaymentState;
import lombok.Getter;

/**
 * 결제 결과 응답 DTO.
 * 서버 → 클라이언트
 */
@Getter
public class PaymentResponseDto {
    private final Long paymentId;
    private final Long orderId;
    private final Long paymentAmount;
    private final PaymentMethod paymentMethod;
    private final PaymentState paymentState;

    public PaymentResponseDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrder().getOrderId();
        this.paymentAmount = payment.getPaymentAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentState = payment.getPaymentState();
    }
}
