package com.example.system_backend.order.application.facade;

import com.example.system_backend.common.dto.CartItemInfo;
import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.common.port.CartClearPort;
import com.example.system_backend.common.port.CartQueryPort;
import com.example.system_backend.order.application.service.OrderCommandService;
import com.example.system_backend.order.application.service.OrderQueryService;
import com.example.system_backend.order.dto.CheckoutResponse;
import com.example.system_backend.order.dto.CreateOrderRequest;
import com.example.system_backend.order.dto.OrderListResponse;
import com.example.system_backend.order.dto.OrderResponse;
import com.example.system_backend.order.dto.UpdateOrderStatusRequest;
import com.example.system_backend.order.dto.OrderStatisticsResponse;
import com.example.system_backend.common.port.ProductQueryPort;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.order.entity.Order;
import com.example.system_backend.order.entity.OrderDetail;
import com.example.system_backend.order.mapper.OrderMapper;
import com.example.system_backend.payment.application.facade.PaymentFacade;
import com.example.system_backend.payment.dto.PaymentMethodDto;
import com.example.system_backend.common.port.StockQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderFacade orchestrates order operations across multiple services.
 * Handles cross-domain coordination and DTO mapping.
 */
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final OrderMapper orderMapper;
    private final ProductQueryPort productQueryPort;
    private final CartQueryPort cartQueryPort;
    private final CartClearPort cartClearPort;
    private final PaymentFacade paymentFacade;
    private final StockQueryPort stockQueryPort;

    /**
     * Create new order - orchestrates validation and creation
     */
    @Transactional
    public OrderResponse createOrder(Integer userId, CreateOrderRequest request) {
        // Validate all products exist and are available
        validateProductsAvailability(request);
        
        // Check stock availability
        validateStockAvailability(request);

        Order savedOrder = orderCommandService.createOrder(
                userId,
                request,
                this::getProductPrice);

        return orderMapper.mapToOrderResponse(savedOrder);
    }

    /**
     * Validate that all products in the order exist and are available
     */
    private void validateProductsAvailability(CreateOrderRequest request) {
        for (var item : request.getItems()) {
            Integer productId = item.getProductId();

            if (!productQueryPort.isProductAvailable(productId)) {
                throw new ValidationException("Product with ID " + productId + " is not available");
            }
        }
    }

    /**
     * Validate stock availability for order items
     */
    private void validateStockAvailability(CreateOrderRequest request) {
        for (var item : request.getItems()) {
            if (!stockQueryPort.hasStock(item.getProductId(), item.getQuantity())) {
                throw new ValidationException("Insufficient stock for product ID: " + item.getProductId(), "INSUFFICIENT_STOCK");
            }
        }
    }

    /**
     * Get product price - used as Function for OrderCommandService
     */
    private BigDecimal getProductPrice(Integer productId) {
        return productQueryPort.getProductPrice(productId)
                .orElseThrow(() -> new ValidationException("Product price not found for ID: " + productId));
    }

    /**
     * Get user's order by ID
     */
    public OrderResponse getUserOrder(Integer userId, Integer orderId) {
        Order order = orderQueryService.getOrderByIdAndUserId(orderId, userId);
        return orderMapper.mapToOrderResponse(order);
    }

    /**
     * Get user's orders with pagination
     */
    public Page<OrderListResponse> getUserOrders(Integer userId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;
        if (status != null) {
            orders = orderQueryService.getUserOrdersByStatus(userId, status, pageable);
        } else {
            orders = orderQueryService.getUserOrders(userId, pageable);
        }

        return orders.map(orderMapper::mapToOrderListResponse);
    }

    /**
     * Cancel order by user
     */
    @Transactional
    public OrderResponse cancelOrder(Integer userId, Integer orderId) {
        Order order = orderQueryService.getOrderByIdAndUserId(orderId, userId);
        Order cancelledOrder = orderCommandService.cancelOrderByUser(order);
        return orderMapper.mapToOrderResponse(cancelledOrder);
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Get order by ID (Admin)
     */
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderQueryService.getOrderById(orderId);
        return orderMapper.mapToOrderResponse(order);
    }

    /**
     * Get all orders with filters (Admin)
     */
    public Page<OrderListResponse> getAllOrders(OrderStatus status, Integer userId,
            LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderQueryService.getOrdersWithFilters(status, userId, startDate, endDate, pageable);
        return orders.map(orderMapper::mapToOrderListResponse);
    }

    /**
     * Update order status (Admin)
     */
    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request) {
        Order order = orderQueryService.getOrderById(orderId);
        Order updatedOrder = orderCommandService.updateOrderStatus(order, request.getStatus());
        return orderMapper.mapToOrderResponse(updatedOrder);
    }

    /**
     * Get order statistics (Admin)
     */
    public OrderStatisticsResponse getOrderStatistics() {
        OrderStatisticsResponse stats = new OrderStatisticsResponse();

        long pendingCount = orderQueryService.countOrdersByStatus(OrderStatus.PENDING);
        long processingCount = orderQueryService.countOrdersByStatus(OrderStatus.PROCESSING);
        long shippedCount = orderQueryService.countOrdersByStatus(OrderStatus.SHIPPED);
        long deliveredCount = orderQueryService.countOrdersByStatus(OrderStatus.DELIVERED);
        long cancelledCount = orderQueryService.countOrdersByStatus(OrderStatus.CANCELLED);

        stats.setPendingCount(pendingCount);
        stats.setProcessingCount(processingCount);
        stats.setShippedCount(shippedCount);
        stats.setDeliveredCount(deliveredCount);
        stats.setCancelledCount(cancelledCount);
        stats.setTotalCount(pendingCount + processingCount + shippedCount + deliveredCount + cancelledCount);

        return stats;
    }

    // ==================== CHECKOUT OPERATIONS ====================

    /**
     * Checkout cart - create order from cart items and prepare for payment
     * This is the proper place for checkout logic as it creates an Order
     */
    @Transactional
    public CheckoutResponse checkoutCart(Integer userId) {
        return checkoutCartWithPaymentMethod(userId, PaymentMethodDto.FAKE);
    }

    /**
     * Checkout cart with specific payment method
     */
    @Transactional
    public CheckoutResponse checkoutCartWithPaymentMethod(Integer userId, PaymentMethodDto paymentMethodDto) {
        // Validate cart has items
        if (!cartQueryPort.hasCartItems(userId)) {
            throw new ValidationException("Cart is empty", "CART_EMPTY");
        }

        // Get cart items
        List<CartItemInfo> cartItems = cartQueryPort.getCartItemsForCheckout(userId);

        // Validate stock availability for cart items
        validateCartStockAvailability(cartItems);

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
            .map(CartItemInfo::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);

        // Save order and get ID
        Order savedOrder = orderCommandService.saveOrder(order);

        // Create order details
        List<OrderDetail> orderDetails = cartItems.stream()
            .map(item -> createOrderDetailFromCartItem(savedOrder, item))
            .collect(Collectors.toList());

        // Set order details
        savedOrder.setOrderDetails(orderDetails);
        orderCommandService.saveOrder(savedOrder);

        // Create payment record (INIT status)
        var paymentResponse = paymentFacade.createPayment(savedOrder.getOrderId(), paymentMethodDto, totalAmount);

        // Clear cart after successful order creation
        cartClearPort.clearUserCart(userId);

        return CheckoutResponse.builder()
            .orderId(savedOrder.getOrderId())
            .totalAmount(totalAmount)
            .status("PENDING")
            .message("Order created successfully. Ready for payment.")
            .paymentId(paymentResponse.getPaymentId())
            .paymentMethod(paymentMethodDto)
            .paymentStatus(paymentResponse.getStatus())
            .build();
    }

    /**
     * Validate stock availability for cart items
     */
    private void validateCartStockAvailability(List<CartItemInfo> cartItems) {
        for (CartItemInfo item : cartItems) {
            if (!stockQueryPort.hasStock(item.getProductId(), item.getQuantity())) {
                throw new ValidationException("Insufficient stock for product ID: " + item.getProductId() + " in cart", "INSUFFICIENT_STOCK");
            }
        }
    }

    /**
     * Create OrderDetail from CartItemInfo
     */
    private OrderDetail createOrderDetailFromCartItem(Order order, CartItemInfo cartItem) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setProductId(cartItem.getProductId());
        orderDetail.setQuantity(cartItem.getQuantity());
        orderDetail.setUnitPrice(cartItem.getUnitPrice());
        return orderDetail;
    }
}