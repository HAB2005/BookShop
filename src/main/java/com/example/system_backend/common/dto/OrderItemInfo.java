package com.example.system_backend.common.dto;

import java.math.BigDecimal;

/**
 * Data structure for order item information
 * Used for cross-module communication
 */
public interface OrderItemInfo {
    Integer getProductId();
    Integer getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getSubtotal();
}