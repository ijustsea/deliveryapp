package com.example.delivery.payment.entity;

/**
 * 주문당 CONFIRM은 최대 1회 허용, FAIL은 여러번 허용
 * 해당 제약은 PostgreSQL Partial Unique Index로 적용
 */
public enum PaymentState {
    CONFIRM,
    FAIL
}
