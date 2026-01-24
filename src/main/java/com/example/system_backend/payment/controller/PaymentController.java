package com.example.system_backend.payment.controller;

import com.example.system_backend.common.response.SuccessResponse;
import com.example.system_backend.payment.application.facade.PaymentFacade;
import com.example.system_backend.payment.dto.PaymentResponse;
import com.example.system_backend.payment.dto.ProcessPaymentRequest;
import com.example.system_backend.payment.entity.Payment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentController handles payment operations for users
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    /**
     * Get payment information for an order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<SuccessResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable Integer orderId,
            Authentication authentication) {
        
        Integer userId = Integer.valueOf(authentication.getName());
        log.info("User {} getting payment for order: {}", userId, orderId);
        
        // TODO: Add authorization check - user can only see their own order payments
        PaymentResponse payment = paymentFacade.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }

    /**
     * Process payment for an order
     */
    @PostMapping("/process")
    public ResponseEntity<SuccessResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            Authentication authentication) {
        
        Integer userId = Integer.valueOf(authentication.getName());
        log.info("User {} processing payment for order: {}", userId, request.getOrderId());
        
        // TODO: Add authorization check - user can only pay for their own orders
        PaymentResponse payment = paymentFacade.processPayment(request);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<SuccessResponse<PaymentResponse>> getPaymentById(
            @PathVariable Integer paymentId,
            Authentication authentication) {
        
        Integer userId = Integer.valueOf(authentication.getName());
        log.info("User {} getting payment: {}", userId, paymentId);
        
        // TODO: Add authorization check - user can only see their own payments
        PaymentResponse payment = paymentFacade.getPaymentById(paymentId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }

    /**
     * Cancel payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<SuccessResponse<PaymentResponse>> cancelPayment(
            @PathVariable Integer paymentId,
            Authentication authentication) {
        
        Integer userId = Integer.valueOf(authentication.getName());
        log.info("User {} cancelling payment: {}", userId, paymentId);
        
        // TODO: Add authorization check - user can only cancel their own payments
        PaymentResponse payment = paymentFacade.cancelPayment(paymentId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }
}