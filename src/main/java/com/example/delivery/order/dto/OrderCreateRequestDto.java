package com.example.delivery.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OrderCreateRequestDto	CUSTOMER	메뉴 ID, 수량, 배송지 입력
 *
 * CUSTOMER 주문 생성 시 menuId, counts, deliveryAddress를 받는 요청 DTO.
 * Validation으로 입력값을 검증하고, 인증된 Access Token을 통해 주문자를 식별.
 * Service에서 User와 Menu를 DB 조회하고, 메뉴 가격과 수량으로 총액 계산.
 * Entity 생성 시 주문 초기 상태는 PENDING으로 설정.
 */
@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {
    @NotNull
    private Long menuId;

    @NotNull
    @Positive
    private Integer counts;

    @NotBlank
    @Size(max = 255)
    private String deliveryAddress;

}
