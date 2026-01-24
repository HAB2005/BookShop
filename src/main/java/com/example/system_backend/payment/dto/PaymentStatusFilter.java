package com.example.system_backend.payment.dto;

/**
 * Payment status filter for API requests
 * Mirrors Payment.PaymentStatus but in DTO layer
 */
public enum PaymentStatusFilter {
    INIT,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED
}