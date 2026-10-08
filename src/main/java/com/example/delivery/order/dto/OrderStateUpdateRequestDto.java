package com.example.delivery.order.dto;

import com.example.delivery.order.entity.OrderState;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OrderStateUpdateRequestDto	OWNER	목표 상태 ACCEPTED 또는 COMPLETED 전달
 *
 * OWNER가 주문 상태 변경 시 목표 상태 하나를 전달하는 요청 DTO.
 * 변경 가능한 목표 상태는 ACCEPTED(주문 수락), COMPLETED(배달 완료).
 * 현재 상태는 DB의 Order Entity에서 조회하므로 DTO에는 목표 상태만 받음.
 * Service에서 OWNER 소유권과 목표 상태를 검사하고 해당 Entity 메서드를 호출.
 * Entity의 validateState()가 현재 상태를 검사하여 허용된 경우에만 변경.
 * 변경된 상태는 Transaction 내 JPA Dirty Checking으로 DB에 반영.
 */
@Getter
@NoArgsConstructor
public class OrderStateUpdateRequestDto {

    @NotNull
    private OrderState orderState;
}