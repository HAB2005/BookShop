package com.example.system_backend.order.dto;

import com.example.system_backend.payment.dto.PaymentMethodDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutWithPaymentRequest {
    
    @NotNull(message = "Payment method is required")
    private PaymentMethodDto paymentMethod;
    
    // Optional fields for specific payment methods
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private String phoneNumber; // For MoMo
    private String email; // For PayPal
}