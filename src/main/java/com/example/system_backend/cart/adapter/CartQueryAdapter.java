package com.example.system_backend.cart.adapter;

import com.example.system_backend.cart.application.service.CartQueryService;
import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.common.dto.CartItemInfo;
import com.example.system_backend.common.port.CartQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements CartQueryPort interface
 * Allows order module to query cart data without direct dependency on cart module
 */
@Component
@RequiredArgsConstructor
public class CartQueryAdapter implements CartQueryPort {
    
    private final CartQueryService cartQueryService;
    
    @Override
    public List<CartItemInfo> getCartItemsForCheckout(Integer userId) {
        Optional<Cart> cartOpt = cartQueryService.getCartByUserId(userId);
        
        if (cartOpt.isEmpty() || cartOpt.get().getCartItems() == null) {
            return List.of();
        }
        
        return cartOpt.get().getCartItems().stream()
            .map(this::convertToCartItemData)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasCartItems(Integer userId) {
        return cartQueryService.getCartByUserId(userId)
            .map(cart -> cart.getCartItems() != null && !cart.getCartItems().isEmpty())
            .orElse(false);
    }
    
    /**
     * Convert CartItem to CartItemInfo
     */
    private CartItemInfo convertToCartItemData(CartItem cartItem) {
        return new CartItemInfo() {
            @Override
            public Integer getProductId() {
                return cartItem.getProductId();
            }
            
            @Override
            public Integer getQuantity() {
                return cartItem.getQuantity();
            }
            
            @Override
            public BigDecimal getUnitPrice() {
                return cartItem.getUnitPrice();
            }
            
            @Override
            public BigDecimal getSubtotal() {
                return cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            }
        };
    }
}