package com.example.system_backend.order.adapter;

import com.example.system_backend.common.dto.OrderItemInfo;
import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.common.port.OrderCreationPort;
import com.example.system_backend.order.entity.Order;
import com.example.system_backend.order.entity.OrderDetail;
import com.example.system_backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that implements OrderCreationPort interface
 * Allows cart module to create orders without direct dependency on order module
 * Follows Hexagonal Architecture pattern
 */
@Component
@RequiredArgsConstructor
public class OrderCreationAdapter implements OrderCreationPort {
    
    private final OrderRepository orderRepository;
    
    /**
     * Create order from cart items
     */
    @Override
    @Transactional
    public Integer createOrderFromCart(Integer userId, List<OrderItemInfo> orderItems, BigDecimal totalAmount) {
        // Create order entity
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        
        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);
        
        // Create order details
        List<OrderDetail> orderDetails = orderItems.stream()
            .map(item -> createOrderDetail(savedOrder, item))
            .collect(Collectors.toList());
        
        // Set order details
        savedOrder.setOrderDetails(orderDetails);
        
        // Save again with details (cascade will save order details)
        orderRepository.save(savedOrder);
        
        return savedOrder.getOrderId();
    }
    
    /**
     * Create OrderDetail from OrderItemInfo
     */
    private OrderDetail createOrderDetail(Order order, OrderItemInfo itemData) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setProductId(itemData.getProductId());
        orderDetail.setQuantity(itemData.getQuantity());
        orderDetail.setUnitPrice(itemData.getUnitPrice());
        // Subtotal will be calculated by database trigger or computed column
        return orderDetail;
    }
}