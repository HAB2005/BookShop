package com.example.system_backend.order.controller;

import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.common.response.SuccessResponse;
import com.example.system_backend.common.util.AuthenticationUtil;
import com.example.system_backend.order.application.facade.OrderFacade;
import com.example.system_backend.order.dto.CheckoutResponse;
import com.example.system_backend.order.dto.CheckoutWithPaymentRequest;
import com.example.system_backend.order.dto.CreateOrderRequest;
import com.example.system_backend.order.dto.OrderListResponse;
import com.example.system_backend.order.dto.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OrderController handles user order operations.
 * Users can only access their own orders.
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;
    private final AuthenticationUtil authenticationUtil;

    /**
     * Create new order
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {

        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
        log.info("Creating order for user: {}", userId);

        OrderResponse order = orderFacade.createOrder(userId, request);
        return ResponseEntity.ok(SuccessResponse.success(order));
    }

    /**
     * Get user's orders with pagination and optional status filter
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<OrderListResponse>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            HttpServletRequest httpRequest) {

        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
        Page<OrderListResponse> orders = orderFacade.getUserOrders(userId, status, page, size);
        PageResponse<OrderListResponse> pageResponse = PageResponse.of(orders);

        return ResponseEntity.ok(SuccessResponse.success(pageResponse));
    }

    /**
     * Get specific order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<SuccessResponse<OrderResponse>> getOrder(
            @PathVariable Integer orderId,
            HttpServletRequest httpRequest) {

        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
        OrderResponse order = orderFacade.getUserOrder(userId, orderId);

        return ResponseEntity.ok(SuccessResponse.success(order));
    }

    /**
     * Cancel order
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<SuccessResponse<OrderResponse>> cancelOrder(
            @PathVariable Integer orderId,
            HttpServletRequest httpRequest) {

        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
        log.info("User {} cancelling order: {}", userId, orderId);

        OrderResponse order = orderFacade.cancelOrder(userId, orderId);
        return ResponseEntity.ok(SuccessResponse.success(order));
    }

    /**
     * Checkout cart - create order from cart items
     * This endpoint properly belongs in Order module as it creates an Order
     */
    @PostMapping("/checkout")
    public ResponseEntity<SuccessResponse<CheckoutResponse>> checkout(HttpServletRequest httpRequest) {
        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
        log.info("User {} checking out cart", userId);

        CheckoutResponse response = orderFacade.checkoutCart(userId);
        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Checkout cart with specific payment method
     */
    @PostMapping("/checkout-with-payment")
    public ResponseEntity<SuccessResponse<CheckoutResponse>> checkoutWithPayment(
            @Valid @RequestBody CheckoutWithPaymentRequest request,
            HttpServletRequest httpRequest) {

        Integer userId = authenticationUtil.getUserIdFromRequest(httpRequest);
        log.info("User {} checking out cart with payment method: {}", userId, request.getPaymentMethod());

        CheckoutResponse response = orderFacade.checkoutCartWithPaymentMethod(userId, request.getPaymentMethod());
        return ResponseEntity.ok(SuccessResponse.success(response));
    }
}