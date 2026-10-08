package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentState;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository를 interface로 선언하고 JpaRepository를 상속하면
 *  Spring Data JPA가 DB 접근 기능을 제공하는 구현 객체를 자동 생성하고
 *  Bean으로 등록하여, Service에서 SQL 구현 없이 CRUD와 Query Method를 사용할 수 있는 것.
 *
 *  즉, interface는 필요한 DB 작업을 선언하고, Spring Data JPA는 구현하고, Spring Container는 그 객체를 관리 및 주입하는 역할
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문에 특정 결제 상태의 기록이 존재하는지 확인
    boolean existsByOrderOrderIdAndPaymentState(
            Long orderId, PaymentState paymentState
    );
}
