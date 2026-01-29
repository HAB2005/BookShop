package com.example.system_backend.order.application.service;

import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.order.domain.OrderValidationService;
import com.example.system_backend.order.dto.CreateOrderRequest;
import com.example.system_backend.order.dto.OrderItemRequest;
import com.example.system_backend.order.domain.OrderItemData;
import com.example.system_backend.order.entity.Order;
import com.example.system_backend.order.entity.OrderDetail;
import com.example.system_backend.order.repository.OrderRepository;
import com.example.system_backend.order.repository.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * OrderCommandService handles ONLY Order-related write operations.
 * Pure CQRS - uses domain validation services.
 */
@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderValidationService orderValidationService;

    /**
     * Create new order - uses domain validation service
     */
    @Transactional
    public Order createOrder(Integer userId, CreateOrderRequest request, 
                           Function<Integer, BigDecimal> productPriceProvider) {
        
        // Convert DTO to domain data
        List<OrderItemData> orderItemsData = request.getItems().stream()
                .map(item -> new OrderItemData(item.getProductId(), item.getQuantity()))
                .collect(java.util.stream.Collectors.toList());

        // Validate using domain service (basic validation only)
        orderValidationService.validateOrderItems(orderItemsData);

        // Calculate total amount using domain service
        BigDecimal totalAmount = orderValidationService.calculateTotalAmount(
            orderItemsData, 
            productPriceProvider
        );

        // Create order entity
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);

        // Create order details
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(itemRequest.getProductId());
            orderDetail.setQuantity(itemRequest.getQuantity());
            
            BigDecimal unitPrice = productPriceProvider.apply(itemRequest.getProductId());
            orderDetail.setUnitPrice(unitPrice);
            orderDetail.setOrder(order);
            
            orderDetails.add(orderDetail);
        }
        
        order.setOrderDetails(orderDetails);
        return orderRepository.save(order);
    }

    /**
     * Update order status - uses domain validation service
     */
    @Transactional
    public Order updateOrderStatus(Order order, OrderStatus newStatus) {
        orderValidationService.updateOrderStatus(order, newStatus);
        return orderRepository.save(order);
    }

    /**
     * Cancel order by user - uses domain validation service
     */
    @Transactional
    public Order cancelOrderByUser(Order order) {
        orderValidationService.validateUserCanCancelOrder(order);
        orderValidationService.updateOrderStatus(order, OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Save order
     */
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Save order detail
     */
    @Transactional
    public OrderDetail saveOrderDetail(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }
}