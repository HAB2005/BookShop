package com.example.system_backend.cart.application.service;

import com.example.system_backend.cart.domain.CartValidationService;
import com.example.system_backend.cart.dto.AddCartItemRequest;
import com.example.system_backend.cart.dto.UpdateCartItemRequest;
import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.cart.repository.CartItemRepository;
import com.example.system_backend.cart.repository.CartRepository;
import com.example.system_backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cart Command Service - handles write operations for cart domain
 * Part of CQRS pattern - only write operations
 */
@Service
@RequiredArgsConstructor
public class CartCommandService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartValidationService cartValidationService;

    /**
     * Create new cart for user
     */
    @Transactional
    public Cart createCart(Integer userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    /**
     * Add item to cart
     */
    @Transactional
    public CartItem addItemToCart(Cart cart, Product product, AddCartItemRequest request) {
        // Validate product and quantity
        cartValidationService.validateProductForCart(product);
        cartValidationService.validateQuantity(request.getQuantity());

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), request.getProductId());

        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            cartValidationService.validateQuantity(newQuantity);

            item.setQuantity(newQuantity);
            item.setUnitPrice(product.getPrice()); // Update price in case it changed
            return cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            return cartItemRepository.save(newItem);
        }
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartItem updateCartItem(CartItem cartItem, UpdateCartItemRequest request) {
        cartValidationService.validateQuantity(request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        return cartItemRepository.save(cartItem);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public void removeCartItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    /**
     * Clear all items from cart
     */
    @Transactional
    public void clearCart(Integer cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }

    /**
     * Save cart
     */
    @Transactional
    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }
}