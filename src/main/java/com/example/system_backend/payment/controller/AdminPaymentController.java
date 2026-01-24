package com.example.system_backend.payment.controller;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.common.response.SuccessResponse;
import com.example.system_backend.payment.application.facade.PaymentFacade;
import com.example.system_backend.payment.application.service.PaymentQueryService;
import com.example.system_backend.payment.dto.PaymentResponse;
import com.example.system_backend.payment.dto.PaymentStatusFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminPaymentController handles payment management operations for admins
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentFacade paymentFacade;

    /**
     * Get all payments with pagination
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin getting all payments: page={}, size={}, sortBy={}, sortDir={}", 
                page, size, sortBy, sortDir);
        
        Page<PaymentResponse> payments = paymentFacade.getAllPayments(page, size, sortBy, sortDir);
        PageResponse<PaymentResponse> pageResponse = PageResponse.of(payments);
        
        return ResponseEntity.ok(SuccessResponse.success(pageResponse));
    }

    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<SuccessResponse<List<PaymentResponse>>> getPaymentsByStatus(
            @PathVariable PaymentStatusFilter status) {
        
        log.info("Admin getting payments by status: {}", status);
        
        // Convert DTO enum to entity enum using mapper or service
        List<PaymentResponse> payments = paymentFacade.getPaymentsByStatusFilter(status);
        return ResponseEntity.ok(SuccessResponse.success(payments));
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<SuccessResponse<PaymentResponse>> getPaymentById(@PathVariable Integer paymentId) {
        log.info("Admin getting payment: {}", paymentId);
        PaymentResponse payment = paymentFacade.getPaymentById(paymentId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<SuccessResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Integer orderId) {
        log.info("Admin getting payment for order: {}", orderId);
        PaymentResponse payment = paymentFacade.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }

    /**
     * Cancel payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<SuccessResponse<PaymentResponse>> cancelPayment(@PathVariable Integer paymentId) {
        log.info("Admin cancelling payment: {}", paymentId);
        PaymentResponse payment = paymentFacade.cancelPayment(paymentId);
        return ResponseEntity.ok(SuccessResponse.success(payment));
    }

    /**
     * Get payment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SuccessResponse<PaymentQueryService.PaymentStatistics>> getPaymentStatistics() {
        log.info("Admin getting payment statistics");
        PaymentQueryService.PaymentStatistics statistics = paymentFacade.getPaymentStatistics();
        return ResponseEntity.ok(SuccessResponse.success(statistics));
    }
}