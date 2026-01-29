package com.example.system_backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for cart item information used in cross-domain operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemInfo {
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}