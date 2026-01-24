package com.example.system_backend.payment.dto;

/**
 * Payment status enum for DTOs
 */
public enum PaymentStatusDto {
    INIT, // Khởi tạo
    PENDING, // Đang chờ xử lý
    SUCCESS, // Thành công
    FAILED, // Thất bại
    CANCELLED // Đã hủy
}