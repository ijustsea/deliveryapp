package com.example.delivery.order.entity;
/**
 * [OrderState 상태 변경 규칙]
 *
 * 상태 전이                | 변경 주체  | 처리 위치
 * ----------------------------------------------------------
 * 생성 → PENDING           | 서버       | Order 생성자
 * PENDING → CANCELED      | CUSTOMER  | OrderService → cancel()
 * PENDING → PAID          | 결제 성공  | PaymentService → markPaid()
 * PAID → ACCEPTED         | OWNER     | OrderService → accept()
 * ACCEPTED → COMPLETED    | OWNER     | OrderService → complete()
 *
 * Service → 권한·소유권 검사 및 상태 변경 메서드 호출
 * Entity  → validateState()로 현재 상태를 검증한 뒤 변경
 * JPA     → Transaction 내 Dirty Checking으로 DB 반영
 *
 * 주문 생성은 결제와 별도 Transaction.
 * 결제 성공 시 Payment 저장 + Order PAID 변경은 같은 DB Transaction.
 */
public enum OrderState {
    PENDING,
    PAID,
    ACCEPTED,
    COMPLETED,
    CANCELED
}
