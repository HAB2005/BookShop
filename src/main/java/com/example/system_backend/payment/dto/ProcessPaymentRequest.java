package com.example.system_backend.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotNull(message = "Order ID is required")
    private Integer orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethodDto method;

    // Optional fields for specific payment methods
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private String phoneNumber; // For MoMo
    private String email; // For PayPal
}