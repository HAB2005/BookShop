package com.example.system_backend.cart.application.facade;

import com.example.system_backend.cart.application.service.CartCommandService;
import com.example.system_backend.cart.application.service.CartQueryService;
import com.example.system_backend.cart.domain.CartValidationService;
import com.example.system_backend.cart.dto.AddCartItemRequest;
import com.example.system_backend.cart.dto.CartResponse;


import com.example.system_backend.cart.dto.UpdateCartItemRequest;
import com.example.system_backend.cart.entity.Cart;
import com.example.system_backend.cart.entity.CartItem;
import com.example.system_backend.cart.mapper.CartMapper;
import com.example.system_backend.common.exception.ResourceNotFoundException;

import com.example.system_backend.product.application.service.ProductQueryService;
import com.example.system_backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cart Facade - orchestrates cross-domain operations between Cart and Product domains
 * Handles complex workflows and coordinates multiple services
 */
@Service
@RequiredArgsConstructor
public class CartFacade {
    
    // Domain services
    private final CartQueryService cartQueryService;
    private final CartCommandService cartCommandService;
    private final CartValidationService cartValidationService;
    
    // Cross-domain services
    private final ProductQueryService productQueryService;
    
    // Mappers
    private final CartMapper cartMapper;
    
    /**
     * Get user's cart with product information
     */
    @Transactional
    public CartResponse getCart(Integer userId) {
        Cart cart = cartQueryService.getOrCreateCartByUserId(userId);
        
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return cartMapper.mapToCartResponse(cart, Map.of());
        }
        
        // Get product information for all cart items
        List<Integer> productIds = cart.getCartItems().stream()
            .map(CartItem::getProductId)
            .collect(Collectors.toList());
        
        Map<Integer, Product> productMap = productQueryService.getProductsByIds(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        
        return cartMapper.mapToCartResponse(cart, productMap);
    }
    
    /**
     * Add item to cart
     */
    @Transactional
    public CartResponse addItemToCart(Integer userId, AddCartItemRequest request) {
        // Get or create cart
        Cart cart = cartQueryService.getOrCreateCartByUserId(userId);
        
        // Get product information (cross-domain call)
        Product product = productQueryService.getProductById(request.getProductId());
        
        // TODO: Validate stock availability when stock management is implemented
        // validateStockAvailability(request.getProductId(), request.getQuantity(), cart.getCartId());
        
        // Add item to cart
        cartCommandService.addItemToCart(cart, product, request);
        
        // Return updated cart
        return getCart(userId);
    }
    
    /**
     * Update cart item quantity
     */
    @Transactional
    public CartResponse updateCartItem(Integer userId, Integer cartItemId, UpdateCartItemRequest request) {
        // Get cart item and validate ownership
        CartItem cartItem = cartQueryService.getCartItemById(cartItemId);
        cartValidationService.validateCartItemForUpdate(cartItem, userId);
        
        // TODO: Validate stock availability when stock management is implemented
        // validateStockAvailability(cartItem.getProductId(), request.getQuantity(), cartItem.getCart().getCartId());
        
        // Update cart item
        cartCommandService.updateCartItem(cartItem, request);
        
        // Return updated cart
        return getCart(userId);
    }
    
    /**
     * Remove item from cart
     */
    @Transactional
    public CartResponse removeCartItem(Integer userId, Integer cartItemId) {
        // Get cart item and validate ownership
        CartItem cartItem = cartQueryService.getCartItemById(cartItemId);
        cartValidationService.validateCartItemForUpdate(cartItem, userId);
        
        // Remove cart item
        cartCommandService.removeCartItem(cartItem);
        
        // Return updated cart
        return getCart(userId);
    }
    
    /**
     * Clear cart
     */
    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = cartQueryService.getCartByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        
        cartCommandService.clearCart(cart.getCartId());
    }
    
    
    /**
     * TODO: Implement stock validation when stock management feature is ready
     * Validate stock availability for cart operations
     */
    /*
    private void validateStockAvailability(Integer productId, Integer requestedQuantity, Integer cartId) {
        // Get current stock
        Stock stock = stockQueryService.getStockByProductIdOrNull(productId);
        
        // Auto-create stock if not exists (for products without stock configuration)
        if (stock == null) {
            try {
                // Create default stock with reasonable quantity
                stock = stockCommandService.createStock(productId, 100, 5);
                System.out.println("Auto-created stock for product " + productId + " with 100 units");
            } catch (Exception e) {
                throw new BusinessException("Product stock not available and cannot be created");
            }
        }
        
        // Calculate total quantity needed (new quantity, not additional)
        int totalQuantityNeeded = requestedQuantity;
        
        // Check if enough stock available
        if (!stock.hasStock(totalQuantityNeeded)) {
            throw new BusinessException(
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                    stock.getAvailableQuantity(), totalQuantityNeeded)
            );
        }
    }
    */
}