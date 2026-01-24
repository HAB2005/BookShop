package com.example.system_backend.payment.application.service;

import com.example.system_backend.common.exception.BusinessException;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.payment.entity.Payment;
import com.example.system_backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PaymentCommandService handles write operations for payment data
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;

    /**
     * Create payment for order
     */
    public Payment createPayment(Integer orderId, Payment.PaymentMethod method, BigDecimal amount) {
        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessException("Payment already exists for order ID: " + orderId);
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.INIT);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment for order {}: {} {}", orderId, amount, method);
        
        return savedPayment;
    }

    /**
     * Process payment (simple fake implementation)
     */
    public Payment processPayment(Integer paymentId) {
        Payment payment = getPaymentById(paymentId);
        
        if (payment.getStatus() != Payment.PaymentStatus.INIT) {
            throw new BusinessException("Payment is not in INIT status. Current status: " + payment.getStatus());
        }

        // Set to pending first
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Simulate payment processing based on method
        boolean success = simulatePaymentProcessing(payment.getMethod());
        
        if (success) {
            String transactionRef = generateTransactionRef();
            payment.markAsSuccessful(transactionRef);
            log.info("Payment {} processed successfully with ref: {}", paymentId, transactionRef);
        } else {
            payment.markAsFailed("Payment processing failed");
            log.warn("Payment {} processing failed", paymentId);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Mark payment as successful (for external payment confirmations)
     */
    public Payment markPaymentAsSuccessful(Integer paymentId, String transactionRef) {
        Payment payment = getPaymentById(paymentId);
        
        if (payment.isSuccessful()) {
            throw new BusinessException("Payment is already successful");
        }

        payment.markAsSuccessful(transactionRef);
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Marked payment {} as successful with ref: {}", paymentId, transactionRef);
        return savedPayment;
    }

    /**
     * Mark payment as failed
     */
    public Payment markPaymentAsFailed(Integer paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);
        
        if (payment.isSuccessful()) {
            throw new BusinessException("Cannot mark successful payment as failed");
        }

        payment.markAsFailed(reason);
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Marked payment {} as failed: {}", paymentId, reason);
        return savedPayment;
    }

    /**
     * Cancel payment
     */
    public Payment cancelPayment(Integer paymentId) {
        Payment payment = getPaymentById(paymentId);
        
        if (payment.isSuccessful()) {
            throw new BusinessException("Cannot cancel successful payment");
        }

        payment.markAsCancelled();
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Cancelled payment {}", paymentId);
        return savedPayment;
    }

    /**
     * Get payment by ID with exception if not found
     */
    private Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
    }

    /**
     * Simulate payment processing based on method
     * In real implementation, this would integrate with actual payment providers
     */
    private boolean simulatePaymentProcessing(Payment.PaymentMethod method) {
        switch (method) {
            case FAKE:
                return true; // Always success for fake payments
            case COD:
                return true; // COD is always successful at this stage
            case MOMO:
            case VNPAY:
            case PAYPAL:
                // Simulate 95% success rate for other methods
                return Math.random() < 0.95;
            default:
                return false;
        }
    }

    /**
     * Generate unique transaction reference
     */
    private String generateTransactionRef() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}