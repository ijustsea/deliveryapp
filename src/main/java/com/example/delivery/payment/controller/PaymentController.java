package com.example.delivery.payment.controller;

import com.example.delivery.global.security.UserDetailsImpl;
import com.example.delivery.payment.dto.PaymentRequestDto;
import com.example.delivery.payment.dto.PaymentResponseDto;
import com.example.delivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/{orderId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Customer 결제 요청
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody PaymentRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
      Long currentUserId = userDetails.getUser().getUserId();

        PaymentResponseDto response = paymentService.processPayment(orderId, currentUserId, request);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
