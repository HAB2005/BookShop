package com.example.system_backend.order.adapter;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.port.OrderQueryPort;
import com.example.system_backend.order.entity.Order;
import com.example.system_backend.order.entity.OrderDetail;
import com.example.system_backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OrderQueryAdapter implements OrderQueryPort to provide order information
 * to other modules without exposing internal implementation
 */
@Component
@RequiredArgsConstructor
public class OrderQueryAdapter implements OrderQueryPort {

    private final OrderRepository orderRepository;

    @Override
    public List<OrderItemInfoPort> getOrderItemsForStockReduction(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return order.getOrderDetails().stream()
                .map(OrderItemInfoImpl::new)
                .map(OrderItemInfoPort.class::cast)
                .toList();
    }

    /**
     * Implementation of OrderItemInfoPort
     */
    private static class OrderItemInfoImpl implements OrderItemInfoPort {
        private final OrderDetail orderDetail;

        public OrderItemInfoImpl(OrderDetail orderDetail) {
            this.orderDetail = orderDetail;
        }

        @Override
        public Integer getProductId() {
            return orderDetail.getProductId();
        }

        @Override
        public Integer getQuantity() {
            return orderDetail.getQuantity();
        }
    }
}