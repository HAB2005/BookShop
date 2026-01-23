package com.example.system_backend.cart.adapter;

import com.example.system_backend.cart.application.service.CartCommandService;
import com.example.system_backend.cart.application.service.CartQueryService;
import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.common.port.CartClearPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adapter that implements CartClearPort interface
 * Allows order module to clear cart without direct dependency on cart module
 */
@Component
@RequiredArgsConstructor
public class CartClearAdapter implements CartClearPort {
    
    private final CartQueryService cartQueryService;
    private final CartCommandService cartCommandService;
    
    @Override
    @Transactional
    public void clearUserCart(Integer userId) {
        Optional<Cart> cartOpt = cartQueryService.getCartByUserId(userId);
        
        if (cartOpt.isPresent()) {
            cartCommandService.clearCart(cartOpt.get().getCartId());
        }
    }
}