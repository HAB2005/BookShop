package com.example.system_backend.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockRequest {
    
    @NotNull(message = "Product ID is required")
    private Integer productId;
    
    @NotNull(message = "Initial quantity is required")
    @Min(value = 0, message = "Initial quantity must be non-negative")
    private Integer initialQuantity;
    
    @Min(value = 0, message = "Low stock threshold must be non-negative")
    @Builder.Default
    private Integer lowStockThreshold = 5;
}