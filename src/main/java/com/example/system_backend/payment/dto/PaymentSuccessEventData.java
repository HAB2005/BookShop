package com.example.system_backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data class for payment success events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEventData {
    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private PaymentMethodDto method;
    private String transactionRef;
}