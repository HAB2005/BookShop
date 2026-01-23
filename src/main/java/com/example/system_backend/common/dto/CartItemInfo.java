package com.example.system_backend.common.dto;

import java.math.BigDecimal;

/**
 * Data structure for cart item information
 * Used for cross-module communication
 */
public interface CartItemInfo {
    Integer getProductId();
    Integer getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getSubtotal();
}