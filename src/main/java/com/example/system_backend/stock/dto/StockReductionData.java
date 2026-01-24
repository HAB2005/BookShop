package com.example.system_backend.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data class for stock reduction items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReductionData {
    private Integer productId;
    private Integer quantity;
}