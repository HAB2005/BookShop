package com.example.system_backend.cart.mapper;

import com.example.system_backend.cart.dto.CartItemResponse;
import com.example.system_backend.cart.dto.CartResponse;
import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.product.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for Cart entities and DTOs
 */
@Component
public class CartMapper {

    /**
     * Map Cart entity to CartResponse DTO
     */
    public CartResponse mapToCartResponse(Cart cart, Map<Integer, Product> productMap) {
        if (cart == null) {
            return null;
        }

        List<CartItemResponse> itemResponses = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                        .map(item -> mapToCartItemResponse(item, productMap.get(item.getProductId())))
                        .collect(Collectors.toList())
                : List.of();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * Map CartItem entity to CartItemResponse DTO
     */
    public CartItemResponse mapToCartItemResponse(CartItem cartItem, Product product) {
        if (cartItem == null) {
            return null;
        }

        String productName = product != null ? product.getName() : "Unknown Product";

        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .productId(cartItem.getProductId())
                .productName(productName)
                .unitPrice(cartItem.getUnitPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .addedAt(cartItem.getAddedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}