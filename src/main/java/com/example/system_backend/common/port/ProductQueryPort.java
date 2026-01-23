package com.example.system_backend.common.port;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * ProductQueryPort defines interface for product query operations.
 * Used by other modules to access product information without direct
 * dependency.
 */
public interface ProductQueryPort {

    /**
     * Get product price by ID
     */
    Optional<BigDecimal> getProductPrice(Integer productId);

    /**
     * Get product name by ID
     */
    Optional<String> getProductName(Integer productId);

    /**
     * Check if product exists and is active
     */
    boolean isProductAvailable(Integer productId);

    /**
     * Get product information for order
     */
    Optional<ProductInfoPort> getProductInfo(Integer productId);

    /**
     * Product information interface for order processing
     */
    interface ProductInfoPort {
        Integer getProductId();

        String getName();

        BigDecimal getPrice();

        boolean isAvailable();
    }
}