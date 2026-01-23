package com.example.system_backend.common.enums;

/**
 * Order status enum - shared across modules
 */
public enum OrderStatus {
    PENDING,     // Chờ xác nhận
    PROCESSING,  // Đang xử lý
    SHIPPED,     // Đã giao vận chuyển
    DELIVERED,   // Đã giao hàng
    CANCELLED;   // Đã hủy

    public static OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }
}