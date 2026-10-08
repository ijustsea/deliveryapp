package com.example.delivery.payment.dto;

import com.example.delivery.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Customer 결제 요청 dto
 * 클라이언트 → 서버
 *
 * 결제수단(paymentMethod) → 클라이언트가 선택
 * 결제금액(paymentAmount) → 서버가 Order.totalAmount에서 조회
 * 결제상태(paymentState) → 서버가 결제 처리 결과에 따라 결정
 *
 * 따라서 결제수단만 클라이언트가 결정할 수 있게 필드구성
 */
@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    @NotNull
    private PaymentMethod paymentMethod;
}
