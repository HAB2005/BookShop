package com.example.system_backend.payment.application.facade;

import com.example.system_backend.common.exception.BusinessException;
import com.example.system_backend.payment.application.service.PaymentCommandService;
import com.example.system_backend.payment.application.service.PaymentQueryService;
import com.example.system_backend.payment.dto.PaymentMethodDto;
import com.example.system_backend.payment.dto.PaymentResponse;
import com.example.system_backend.payment.dto.PaymentStatusFilter;
import com.example.system_backend.payment.dto.PaymentSuccessEventData;
import com.example.system_backend.payment.dto.ProcessPaymentRequest;
import com.example.system_backend.payment.entity.Payment;
import com.example.system_backend.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * PaymentFacade orchestrates payment operations and cross-domain interactions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentQueryService paymentQueryService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Integer orderId) {
        Payment payment = paymentQueryService.getPaymentByOrderId(orderId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Create payment for order
     */
    @Transactional
    public PaymentResponse createPayment(Integer orderId, Payment.PaymentMethod method, BigDecimal amount) {
        Payment payment = paymentCommandService.createPayment(orderId, method, amount);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Create payment for order with DTO enum
     */
    @Transactional
    public PaymentResponse createPayment(Integer orderId, PaymentMethodDto methodDto, BigDecimal amount) {
        Payment.PaymentMethod method = paymentMapper.toPaymentMethod(methodDto);
        return createPayment(orderId, method, amount);
    }

    /**
     * Process payment (main payment flow)
     */
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        // Get or create payment
        Payment payment = paymentQueryService.getPaymentByOrderIdOrNull(request.getOrderId());
        
        if (payment == null) {
            throw new BusinessException("Payment not found for order ID: " + request.getOrderId());
        }

        // Process the payment
        Payment processedPayment = paymentCommandService.processPayment(payment.getPaymentId());
        
        // If payment successful, publish event for stock reduction
        if (processedPayment.isSuccessful()) {
            publishPaymentSuccessEvent(processedPayment);
        }

        return paymentMapper.toResponse(processedPayment);
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Integer paymentId) {
        Payment payment = paymentQueryService.getPaymentById(paymentId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Get all payments with pagination
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentQueryService.getAllPayments(pageable);
        return payments.map(paymentMapper::toResponse);
    }

    /**
     * Get all payments with pagination parameters
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return getAllPayments(pageable);
    }

    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = paymentQueryService.getPaymentsByStatus(status);
        return paymentMapper.toResponseList(payments);
    }

    /**
     * Get payments by status filter (DTO enum)
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatusFilter(PaymentStatusFilter statusFilter) {
        // Convert DTO enum to entity enum
        Payment.PaymentStatus entityStatus = Payment.PaymentStatus.valueOf(statusFilter.name());
        return getPaymentsByStatus(entityStatus);
    }

    /**
     * Cancel payment
     */
    @Transactional
    public PaymentResponse cancelPayment(Integer paymentId) {
        Payment payment = paymentCommandService.cancelPayment(paymentId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Get payment statistics
     */
    @Transactional(readOnly = true)
    public PaymentQueryService.PaymentStatistics getPaymentStatistics() {
        return paymentQueryService.getPaymentStatistics();
    }

    /**
     * Check if order has successful payment
     */
    @Transactional(readOnly = true)
    public boolean hasSuccessfulPayment(Integer orderId) {
        Payment payment = paymentQueryService.getPaymentByOrderIdOrNull(orderId);
        return payment != null && payment.isSuccessful();
    }

    /**
     * Publish payment success event for other modules to handle
     */
    private void publishPaymentSuccessEvent(Payment payment) {
        PaymentSuccessEventData event = PaymentSuccessEventData.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .method(PaymentMethodDto.valueOf(payment.getMethod().name()))
                .transactionRef(payment.getTransactionRef())
                .build();
        
        eventPublisher.publishEvent(event);
        log.info("Published payment success event for order {}", payment.getOrderId());
    }
}