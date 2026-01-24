package com.example.system_backend.payment.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.payment.entity.Payment;
import com.example.system_backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentQueryService handles read operations for payment data
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    /**
     * Get payment by ID
     */
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
    }

    /**
     * Get payment by order ID
     */
    public Payment getPaymentByOrderId(Integer orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));
    }

    /**
     * Get payment by order ID, return null if not found
     */
    public Payment getPaymentByOrderIdOrNull(Integer orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    /**
     * Check if payment exists for order
     */
    public boolean existsByOrderId(Integer orderId) {
        return paymentRepository.findByOrderId(orderId).isPresent();
    }

    /**
     * Get payments by status
     */
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get payments by method
     */
    public List<Payment> getPaymentsByMethod(Payment.PaymentMethod method) {
        return paymentRepository.findByMethodOrderByCreatedAtDesc(method);
    }

    /**
     * Get payment by transaction reference
     */
    public Payment getPaymentByTransactionRef(String transactionRef) {
        return paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ref: " + transactionRef));
    }

    /**
     * Get payments within date range
     */
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get successful payments within date range
     */
    public List<Payment> getSuccessfulPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findSuccessfulPaymentsByDateRange(startDate, endDate);
    }

    /**
     * Get all payments with pagination
     */
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Get payment statistics
     */
    public PaymentStatistics getPaymentStatistics() {
        BigDecimal totalAmount = paymentRepository.getTotalSuccessfulPaymentAmount();
        Long successfulCount = paymentRepository.countByStatus(Payment.PaymentStatus.SUCCESS);
        Long pendingCount = paymentRepository.countByStatus(Payment.PaymentStatus.PENDING);
        Long failedCount = paymentRepository.countByStatus(Payment.PaymentStatus.FAILED);
        
        return PaymentStatistics.builder()
                .totalSuccessfulAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .successfulPayments(successfulCount != null ? successfulCount : 0L)
                .pendingPayments(pendingCount != null ? pendingCount : 0L)
                .failedPayments(failedCount != null ? failedCount : 0L)
                .build();
    }

    /**
     * Get payment statistics for date range
     */
    public PaymentStatistics getPaymentStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalAmount = paymentRepository.getTotalSuccessfulPaymentAmountByDateRange(startDate, endDate);
        List<Payment> payments = paymentRepository.findByDateRange(startDate, endDate);
        
        long successfulCount = payments.stream().filter(Payment::isSuccessful).count();
        long pendingCount = payments.stream().filter(Payment::isPending).count();
        long failedCount = payments.stream().filter(Payment::isFailedOrCancelled).count();
        
        return PaymentStatistics.builder()
                .totalSuccessfulAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .successfulPayments(successfulCount)
                .pendingPayments(pendingCount)
                .failedPayments(failedCount)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentStatistics {
        private BigDecimal totalSuccessfulAmount;
        private Long successfulPayments;
        private Long pendingPayments;
        private Long failedPayments;
    }
}