package com.example.system_backend.order.domain;

/**
 * Domain data class for order item validation.
 * Pure domain object - no dependencies on DTO layer.
 */
public class OrderItemData {
    private final Integer productId;
    private final Integer quantity;

    public OrderItemData(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}