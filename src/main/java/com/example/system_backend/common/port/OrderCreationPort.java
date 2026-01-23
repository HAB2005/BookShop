package com.example.system_backend.common.port;

import com.example.system_backend.common.dto.OrderItemInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Port interface for order creation operations.
 * This allows cart module to create orders without depending on order module
 * directly.
 * Follows Hexagonal Architecture pattern.
 */
public interface OrderCreationPort {

    /**
     * Create order from cart items
     * 
     * @param userId      User ID who owns the cart
     * @param orderItems  List of items to include in the order
     * @param totalAmount Total amount of the order
     * @return Created order ID
     */
    Integer createOrderFromCart(Integer userId, List<OrderItemInfo> orderItems, BigDecimal totalAmount);
}