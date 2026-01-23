package com.example.system_backend.cart.controller;

import com.example.system_backend.cart.application.facade.CartFacade;
import com.example.system_backend.cart.dto.AddCartItemRequest;
import com.example.system_backend.cart.dto.CartResponse;
import com.example.system_backend.cart.dto.UpdateCartItemRequest;
import com.example.system_backend.common.util.AuthenticationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Cart Controller - handles cart-related HTTP requests
 * Follows REST API conventions
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartFacade cartFacade;
    private final AuthenticationUtil authenticationUtil;

    /**
     * GET /api/cart - View cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        CartResponse cart = cartFacade.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items - Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody AddCartItemRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromRequest(httpRequest);
        CartResponse cart = cartFacade.addItemToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * PUT /api/cart/items/{id} - Update quantity
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromRequest(httpRequest);
        CartResponse cart = cartFacade.updateCartItem(userId, id, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/items/{id} - Remove item
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        Integer userId = getUserIdFromRequest(httpRequest);
        CartResponse cart = cartFacade.removeCartItem(userId, id);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart - Clear cart
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        cartFacade.clearCart(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Extract user ID from JWT token in request
     */
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        return authenticationUtil.getUserIdFromRequest(request);
    }
}