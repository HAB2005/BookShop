package com.example.system_backend.payment.controller;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.response.SuccessResponse;
import com.example.system_backend.common.util.AuthenticationUtil;
import com.example.system_backend.payment.application.facade.PaymentFacade;
import com.example.system_backend.payment.dto.PaymentResponse;
import com.example.system_backend.payment.dto.ProcessPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final AuthenticationUtil authenticationUtil;

    /**
     * Get payment information for an order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<SuccessResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable Integer orderId,
            HttpServletRequest request) {

        Integer userId = authenticationUtil.getUserIdFromRequest(request);
        log.info("User {} getting payment for order: {}", userId, orderId);

        try {
            // TODO: Add authorization check - user can only see their own order payments
            PaymentResponse payment = paymentFacade.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(SuccessResponse.success(payment));
        } catch (ResourceNotFoundException e) {
            log.warn("Payment not found for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.ok(SuccessResponse.success(null));
        }
    }

    /**
     * Process payment for an order
     */
    @PostMapping("/process")
    public ResponseEntity<SuccessResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            HttpServletRequest httpRequest) {

        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
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
            HttpServletRequest request) {

        Integer userId = authenticationUtil.getUserIdFromRequest(request);
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
            HttpServletRequest request) {

        Integer userId = authenticationUtil.getUserIdFromRequest(request);
        log.info("User {} cancelling payment: {}", userId, paymentId);

        // TODO: Add authorization check - user can only cancel their own payments
        PaymentResponse payment = paymentFacade.cancelPayment(paymentId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }
}