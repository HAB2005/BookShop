package com.example.system_backend.cart.domain;

import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.entity.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Domain service for cart business logic and validation
 * Contains pure business rules without infrastructure dependencies
 */
@Service
public class CartValidationService {

    /**
     * Validate if product can be added to cart
     */
    public void validateProductForCart(Product product) {
        if (product == null) {
            throw new ValidationException("Product not found", "PRODUCT_NOT_FOUND");
        }

        if (product.getStatus() != Product.Status.ACTIVE) {
            throw new ValidationException("Product is not available for purchase", "PRODUCT_NOT_AVAILABLE");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Product price is invalid", "INVALID_PRODUCT_PRICE");
        }
    }

    /**
     * Validate quantity for cart item
     */
    public void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("Quantity must be greater than 0", "INVALID_QUANTITY");
        }

        if (quantity > 99) {
            throw new ValidationException("Quantity cannot exceed 99", "QUANTITY_LIMIT_EXCEEDED");
        }
    }

    /**
     * Validate cart item for update
     */
    public void validateCartItemForUpdate(CartItem cartItem, Integer userId) {
        if (cartItem == null) {
            throw new ValidationException("Cart item not found", "CART_ITEM_NOT_FOUND");
        }

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to user", "CART_ITEM_ACCESS_DENIED");
        }
    }

    /**
     * Validate cart for checkout
     */
    public void validateCartForCheckout(Cart cart) {
        if (cart == null) {
            throw new ValidationException("Cart not found", "CART_NOT_FOUND");
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new ValidationException("Cart is empty", "CART_EMPTY");
        }

        // Validate all items in cart
        for (CartItem item : cart.getCartItems()) {
            if (item.getQuantity() <= 0) {
                throw new ValidationException("Invalid quantity in cart", "INVALID_CART_ITEM_QUANTITY");
            }

            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Invalid price in cart", "INVALID_CART_ITEM_PRICE");
            }
        }
    }

    /**
     * Calculate cart total amount
     */
    public BigDecimal calculateCartTotal(Cart cart) {
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cart.getCartItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total items in cart
     */
    public Integer calculateTotalItems(Cart cart) {
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return 0;
        }

        return cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}