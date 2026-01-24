package com.example.system_backend.order.dto;

import com.example.system_backend.payment.dto.PaymentMethodDto;
import com.example.system_backend.payment.dto.PaymentStatusDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private Integer orderId;
    private BigDecimal totalAmount;
    private String status;
    private String message;

    // Payment information
    private Integer paymentId;
    private PaymentMethodDto paymentMethod;
    private PaymentStatusDto paymentStatus;
}