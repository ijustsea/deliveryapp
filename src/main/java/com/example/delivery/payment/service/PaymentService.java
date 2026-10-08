package com.example.delivery.payment.service;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderState;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.dto.PaymentRequestDto;
import com.example.delivery.payment.dto.PaymentResponseDto;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentMethod;
import com.example.delivery.payment.entity.PaymentState;
import com.example.delivery.payment.repository.PaymentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService( PaymentRepository paymentRepository, OrderRepository orderRepository ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponseDto processPayment(Long orderId, Long currentUserId, PaymentRequestDto request) {
        // 1.결제할 Order가 존재하는지 orderId로 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found."
                ));
        // 2. 현재 CUSTOMER가 주문의 소유자인지 확인
        if(!order.getUser().getUserId().equals(currentUserId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the order owner access."
            );
        }

        // 3. OrderState must be PENDING : 결제가능한 상태인지 체크
        if (order.getOrderState() != OrderState.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is not pending."
            );
        }
        // 4. 기존 성공 결제 기록 존재 여부 확인
        boolean alreadyPaid = paymentRepository.existsByOrderOrderIdAndPaymentState(
                        orderId,
                        PaymentState.CONFIRM
                );
        // CONFIRM 존재할 경우 결제 이미 되어서 예외처리
        if(alreadyPaid) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order already paid."
            );
        }
        // 5. 결제수단 확인 ( CONFIRM 없는 경우 ) : 카드만 허용
        if (request.getPaymentMethod() != PaymentMethod.CARD) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment method."
            );
        }
        // 6. CARD 결제 성공 기록(CONFIRM) 생성
        Payment payment = new Payment(
                order,
                request.getPaymentMethod(),
                PaymentState.CONFIRM
        );
        // 7. 결제 성공 기록 저장
        Payment savedPayment = paymentRepository.save(payment);
        // 8. 주문 상태 PENDING → PAID로 결제 완료
        try {
            order.markPaid();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage(),
                    e
            );
        }
        // 9. 응답 DTO 반환
        return new PaymentResponseDto(savedPayment);
    }
}
