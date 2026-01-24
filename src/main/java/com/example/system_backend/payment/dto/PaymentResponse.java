package com.example.system_backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer paymentId;
    private Integer orderId;
    private PaymentMethodDto method;
    private BigDecimal amount;
    private PaymentStatusDto status;
    private String transactionRef;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}