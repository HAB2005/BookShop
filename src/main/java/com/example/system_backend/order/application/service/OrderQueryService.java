package com.example.system_backend.order.application.service;

import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.order.entity.Order;
import com.example.system_backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * OrderQueryService handles ONLY Order-related read operations.
 * Pure CQRS - no write operations, returns domain entities.
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * Get order by ID
     */
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    /**
     * Get order by ID and user ID (for security)
     */
    public Order getOrderByIdAndUserId(Integer orderId, Integer userId) {
        return orderRepository.findByOrderIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    /**
     * Get user's orders with pagination
     */
    public Page<Order> getUserOrders(Integer userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
    }

    /**
     * Get user's orders by status with pagination
     */
    public Page<Order> getUserOrdersByStatus(Integer userId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status, pageable);
    }

    /**
     * Get all orders with filters (Admin only)
     */
    public Page<Order> getOrdersWithFilters(OrderStatus status, Integer userId, 
                                          LocalDateTime startDate, LocalDateTime endDate, 
                                          Pageable pageable) {
        return orderRepository.findByFilters(status, userId, startDate, endDate, pageable);
    }

    /**
     * Count orders by status
     */
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Count orders by user
     */
    public long countOrdersByUser(Integer userId) {
        return orderRepository.countByUserId(userId);
    }
}