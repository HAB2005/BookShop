package com.example.system_backend.common.port;

import com.example.system_backend.common.dto.CartItemInfo;

import java.util.List;

/**
 * Port interface for cart query operations.
 * Allows order module to access cart data without depending on cart module directly.
 */
public interface CartQueryPort {
    
    /**
     * Get cart items for checkout
     * 
     * @param userId User ID
     * @return List of cart items ready for checkout
     */
    List<CartItemInfo> getCartItemsForCheckout(Integer userId);
    
    /**
     * Check if user has items in cart
     * 
     * @param userId User ID
     * @return true if cart has items
     */
    boolean hasCartItems(Integer userId);
}