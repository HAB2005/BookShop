package com.example.system_backend.cart.adapter;

import com.example.system_backend.cart.application.service.CartCommandService;
import com.example.system_backend.common.port.CartClearPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements CartClearPort using cart domain services
 */
@Component
@RequiredArgsConstructor
public class CartClearAdapter implements CartClearPort {

    private final CartCommandService cartCommandService;

    @Override
    public void clearUserCart(Integer userId) {
        cartCommandService.clearCart(userId);
    }
}