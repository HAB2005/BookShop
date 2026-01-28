package com.example.system_backend.cart.application.service;

import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.cart.repository.CartItemRepository;
import com.example.system_backend.cart.repository.CartRepository;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cart Query Service - handles read operations for cart domain
 * Part of CQRS pattern - only read operations
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Get cart by user ID with items
     */
    public Optional<Cart> getCartByUserId(Integer userId) {
        return cartRepository.findByUserIdWithItems(userId);
    }

    /**
     * Get cart by user ID, create if not exists
     */
    @Transactional
    public Cart getOrCreateCartByUserId(Integer userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Get cart item by ID
     */
    public CartItem getCartItemById(Integer cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
    }

    /**
     * Find cart item by cart ID and product ID
     */
    public Optional<CartItem> findCartItemByCartAndProduct(Integer cartId, Integer productId) {
        return cartItemRepository.findByCartIdAndProductId(cartId, productId);
    }

    /**
     * Check if cart exists for user
     */
    public boolean cartExistsForUser(Integer userId) {
        return cartRepository.existsByUserId(userId);
    }

    /**
     * Count items in cart
     */
    public Long countItemsInCart(Integer cartId) {
        return cartItemRepository.countByCartId(cartId);
    }
}