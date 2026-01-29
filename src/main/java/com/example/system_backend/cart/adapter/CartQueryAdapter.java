package com.example.system_backend.cart.adapter;

import com.example.system_backend.cart.application.service.CartQueryService;
import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.common.dto.CartItemInfo;
import com.example.system_backend.common.port.CartQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that implements CartQueryPort using cart domain services
 */
@Component
@RequiredArgsConstructor
public class CartQueryAdapter implements CartQueryPort {

    private final CartQueryService cartQueryService;

    @Override
    public List<CartItemInfo> getCartItemsForCheckout(Integer userId) {
        Cart cart = cartQueryService.getOrCreateCartByUserId(userId);
        
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return List.of();
        }
        
        return cart.getCartItems().stream()
                .map(this::mapToCartItemInfo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasCartItems(Integer userId) {
        Cart cart = cartQueryService.getOrCreateCartByUserId(userId);
        return cart.getCartItems() != null && !cart.getCartItems().isEmpty();
    }

    private CartItemInfo mapToCartItemInfo(CartItem cartItem) {
        return CartItemInfo.builder()
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .subtotal(cartItem.getSubtotal())
                .build();
    }
}