package com.example.system_backend.product.domain;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * ProductValidationService encapsulates all product domain validation rules.
 * Pure domain logic - no application concerns.
 */
@Service
@RequiredArgsConstructor
public class ProductValidationService {

    /**
     * Validate product name
     */
    public void validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Product name cannot be empty", "NAME_EMPTY");
        }
        if (name.length() > 100) {
            throw new ValidationException("Product name cannot exceed 100 characters", "NAME_TOO_LONG");
        }
    }

    /**
     * Validate product price
     */
    public void validateProductPrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Price cannot be null", "PRICE_NULL");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than 0", "PRICE_INVALID");
        }
    }

    /**
     * Validate status change
     */
    public void validateStatusChange(Product product, String newStatusString) {
        if (newStatusString == null || newStatusString.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty", "STATUS_EMPTY");
        }

        Product.Status newStatus;
        try {
            newStatus = Product.Status.valueOf(newStatusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status. Must be 'ACTIVE', 'INACTIVE', or 'DELETED'",
                    "INVALID_STATUS");
        }

        validateStatusChange(product, newStatus);
    }

    /**
     * Validate status change with business rules
     */
    public void validateStatusChange(Product product, Product.Status newStatus) {
        if (newStatus == null) {
            throw new ValidationException("Status cannot be null", "STATUS_NULL");
        }

        // Business rule: Can only delete inactive products
        if (newStatus == Product.Status.DELETED && product.getStatus() != Product.Status.INACTIVE) {
            throw new ValidationException("Product must be inactive before deletion", "CANNOT_DELETE_ACTIVE_PRODUCT");
        }
    }

    /**
     * Check if product can be deleted
     */
    public boolean canProductBeDeleted(Product product) {
        return product.getStatus() == Product.Status.INACTIVE;
    }
}
