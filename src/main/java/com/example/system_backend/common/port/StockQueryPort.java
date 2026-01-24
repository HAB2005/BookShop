package com.example.system_backend.common.port;

/**
 * StockQueryPort allows other modules to query stock information
 * without direct dependency on Stock module
 */
public interface StockQueryPort {
    
    /**
     * Check if product has sufficient stock
     */
    boolean hasStock(Integer productId, Integer quantity);
    
    /**
     * Get available quantity for product
     */
    Integer getAvailableQuantity(Integer productId);
    
    /**
     * Check if stock exists for product
     */
    boolean existsByProductId(Integer productId);
}