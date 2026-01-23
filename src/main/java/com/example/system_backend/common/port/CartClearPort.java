package com.example.system_backend.common.port;

/**
 * Port interface for cart clearing operations.
 * Allows order module to clear cart after successful checkout.
 */
public interface CartClearPort {
    
    /**
     * Clear all items from user's cart
     * 
     * @param userId User ID whose cart should be cleared
     */
    void clearUserCart(Integer userId);
}