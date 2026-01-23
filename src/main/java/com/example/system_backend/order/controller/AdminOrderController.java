package com.example.system_backend.order.controller;

import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.common.response.SuccessResponse;
import com.example.system_backend.order.application.facade.OrderFacade;
import com.example.system_backend.order.dto.OrderListResponse;
import com.example.system_backend.order.dto.OrderResponse;
import com.example.system_backend.order.dto.UpdateOrderStatusRequest;
import com.example.system_backend.order.dto.OrderStatisticsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * AdminOrderController handles admin order operations.
 * Admins can access and manage all orders.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderFacade orderFacade;

    /**
     * Get all orders with filters and pagination
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<OrderListResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Page<OrderListResponse> orders = orderFacade.getAllOrders(status, userId, startDate, endDate, page, size);
        PageResponse<OrderListResponse> pageResponse = PageResponse.of(orders);
        
        return ResponseEntity.ok(SuccessResponse.success(pageResponse));
    }

    /**
     * Get specific order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<SuccessResponse<OrderResponse>> getOrder(@PathVariable Integer orderId) {
        OrderResponse order = orderFacade.getOrderById(orderId);
        return ResponseEntity.ok(SuccessResponse.success(order));
    }

    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<SuccessResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Integer orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        
        log.info("Admin updating order {} status to: {}", orderId, request.getStatus());
        OrderResponse order = orderFacade.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(SuccessResponse.success(order));
    }

    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SuccessResponse<OrderStatisticsResponse>> getOrderStatistics() {
        OrderStatisticsResponse statistics = orderFacade.getOrderStatistics();
        return ResponseEntity.ok(SuccessResponse.success(statistics));
    }
}