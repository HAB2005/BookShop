package com.example.system_backend.payment.dto;

/**
 * Payment method enum for DTOs
 */
public enum PaymentMethodDto {
    COD, // Cash on Delivery
    FAKE, // Fake payment for testing
    MOMO, // MoMo wallet
    VNPAY, // VNPay
    PAYPAL // PayPal
}