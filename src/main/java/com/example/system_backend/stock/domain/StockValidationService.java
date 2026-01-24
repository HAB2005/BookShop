package com.example.system_backend.stock.domain;

import com.example.system_backend.common.exception.ValidationException;
import org.springframework.stereotype.Service;

/**
 * StockValidationService handles domain-level validation for stock operations
 */
@Service
public class StockValidationService {

    /**
     * Validate stock quantity
     */
    public void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new ValidationException("Quantity cannot be null", "QUANTITY_NULL");
        }
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative", "QUANTITY_NEGATIVE");
        }
    }

    /**
     * Validate low stock threshold
     */
    public void validateLowStockThreshold(Integer threshold) {
        if (threshold == null) {
            throw new ValidationException("Low stock threshold cannot be null", "THRESHOLD_NULL");
        }
        if (threshold < 0) {
            throw new ValidationException("Low stock threshold cannot be negative", "THRESHOLD_NEGATIVE");
        }
    }

    /**
     * Validate product ID
     */
    public void validateProductId(Integer productId) {
        if (productId == null) {
            throw new ValidationException("Product ID cannot be null", "PRODUCT_ID_NULL");
        }
        if (productId <= 0) {
            throw new ValidationException("Product ID must be positive", "PRODUCT_ID_INVALID");
        }
    }

    /**
     * Validate stock reduction
     */
    public void validateStockReduction(Integer availableQuantity, Integer requestedQuantity) {
        validateQuantity(availableQuantity);
        validateQuantity(requestedQuantity);
        
        if (requestedQuantity > availableQuantity) {
            throw new ValidationException(
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                            availableQuantity, requestedQuantity),
                "INSUFFICIENT_STOCK");
        }
    }
}