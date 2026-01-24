package com.example.system_backend.payment.mapper;

import com.example.system_backend.payment.dto.PaymentMethodDto;
import com.example.system_backend.payment.dto.PaymentResponse;
import com.example.system_backend.payment.dto.PaymentStatusDto;
import com.example.system_backend.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PaymentMapper handles mapping between Payment entities and DTOs
 */
@Component
public class PaymentMapper {

    /**
     * Map Payment entity to PaymentResponse DTO
     */
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .method(toPaymentMethodDto(payment.getMethod()))
                .amount(payment.getAmount())
                .status(toPaymentStatusDto(payment.getStatus()))
                .transactionRef(payment.getTransactionRef())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    /**
     * Map list of Payment entities to list of PaymentResponse DTOs
     */
    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        if (payments == null) {
            return null;
        }

        return payments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity PaymentMethod to DTO PaymentMethodDto
     */
    private PaymentMethodDto toPaymentMethodDto(Payment.PaymentMethod method) {
        if (method == null) {
            return null;
        }
        return PaymentMethodDto.valueOf(method.name());
    }

    /**
     * Convert entity PaymentStatus to DTO PaymentStatusDto
     */
    private PaymentStatusDto toPaymentStatusDto(Payment.PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return PaymentStatusDto.valueOf(status.name());
    }

    /**
     * Convert DTO PaymentMethodDto to entity PaymentMethod
     */
    public Payment.PaymentMethod toPaymentMethod(PaymentMethodDto methodDto) {
        if (methodDto == null) {
            return null;
        }
        return Payment.PaymentMethod.valueOf(methodDto.name());
    }

    /**
     * Convert DTO PaymentStatusDto to entity PaymentStatus
     */
    public Payment.PaymentStatus toPaymentStatus(PaymentStatusDto statusDto) {
        if (statusDto == null) {
            return null;
        }
        return Payment.PaymentStatus.valueOf(statusDto.name());
    }
}