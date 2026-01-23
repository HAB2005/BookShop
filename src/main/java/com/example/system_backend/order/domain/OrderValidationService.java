package com.example.system_backend.order.domain;

import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.order.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * OrderValidationService contains all business logic and validation rules for
 * orders.
 * Pure domain service - no external dependencies.
 */
@Service
public class OrderValidationService {

    /**
     * Validate order items before creating order
     */
    public void validateOrderItems(List<OrderItemData> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Order must contain at least one item");
        }

        for (OrderItemData item : items) {
            if (item.getProductId() == null) {
                throw new ValidationException("Product ID is required for all items");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than 0");
            }
        }
    }

    /**
     * Validate order items with product information
     */
    public void validateOrderItemsWithProducts(List<OrderItemData> items, 
                                             java.util.function.Function<Integer, Boolean> productAvailabilityChecker) {
        validateOrderItems(items);
        
        for (OrderItemData item : items) {
            if (!productAvailabilityChecker.apply(item.getProductId())) {
                throw new ValidationException("Product with ID " + item.getProductId() + " is not available");
            }
        }
    }

    /**
     * Validate order status transition
     */
    public void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            throw new ValidationException("Order is already in " + newStatus + " status");
        }

        // Business rules for status transitions based on database enum
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED) {
                    throw new ValidationException("Order in PENDING status can only be PROCESSING or CANCELLED");
                }
                break;
            case PROCESSING:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new ValidationException("Order in PROCESSING status can only be SHIPPED or CANCELLED");
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new ValidationException("Order in SHIPPED status can only be DELIVERED");
                }
                break;
            case DELIVERED:
                throw new ValidationException("Cannot change status of DELIVERED order");
            case CANCELLED:
                throw new ValidationException("Cannot change status of CANCELLED order");
        }
    }

    /**
     * Validate if order can be cancelled by user
     */
    public void validateUserCanCancelOrder(Order order) {
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.PENDING && status != OrderStatus.PROCESSING) {
            throw new ValidationException("Order can only be cancelled when in PENDING or PROCESSING status");
        }
    }

    /**
     * Calculate total amount for order items
     */
    public BigDecimal calculateTotalAmount(List<OrderItemData> items,
            java.util.function.Function<Integer, BigDecimal> priceProvider) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemData item : items) {
            BigDecimal unitPrice = priceProvider.apply(item.getProductId());
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Invalid price for product ID: " + item.getProductId());
            }

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }

        return total;
    }

    /**
     * Update order status with validation - removed note functionality
     */
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        validateStatusTransition(order, newStatus);
        order.setStatus(newStatus);
    }
}