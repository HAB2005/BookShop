package com.example.system_backend.order.mapper;

import com.example.system_backend.common.port.ProductQueryPort;
import com.example.system_backend.order.dto.OrderDetailResponse;
import com.example.system_backend.order.dto.OrderListResponse;
import com.example.system_backend.order.dto.OrderResponse;
import com.example.system_backend.order.entity.Order;
import com.example.system_backend.order.entity.OrderDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ProductQueryPort productQueryPort;

    public OrderResponse mapToOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());

        if (order.getOrderDetails() != null) {
            List<OrderDetailResponse> detailResponses = order.getOrderDetails().stream()
                    .map(this::mapToOrderDetailResponse)
                    .collect(Collectors.toList());
            response.setDetails(detailResponses);
        }

        return response;
    }

    public OrderListResponse mapToOrderListResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderListResponse response = new OrderListResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());

        if (order.getOrderDetails() != null) {
            response.setItemCount(order.getOrderDetails().size());
        } else {
            response.setItemCount(0);
        }

        return response;
    }

    public OrderDetailResponse mapToOrderDetailResponse(OrderDetail orderDetail) {
        if (orderDetail == null) {
            return null;
        }

        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderDetailId(orderDetail.getOrderDetailId());
        response.setProductId(orderDetail.getProductId());

        // Get product name from ProductQueryPort
        String productName = productQueryPort.getProductName(orderDetail.getProductId())
                .orElse("Product " + orderDetail.getProductId());
        response.setProductName(productName);

        response.setQuantity(orderDetail.getQuantity());
        response.setUnitPrice(orderDetail.getUnitPrice());
        response.setSubtotal(orderDetail.getSubtotal());

        return response;
    }

    public List<OrderListResponse> mapToOrderListResponses(List<Order> orders) {
        return orders.stream()
                .map(this::mapToOrderListResponse)
                .collect(Collectors.toList());
    }
}