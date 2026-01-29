package com.example.system_backend.order.repository;

import com.example.system_backend.common.enums.OrderStatus;
import com.example.system_backend.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

        // Find orders by user ID with pagination
        @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.orderDate DESC")
        Page<Order> findByUserIdOrderByOrderDateDesc(@Param("userId") Integer userId, Pageable pageable);

        // Find orders by user ID and status
        @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.orderDate DESC")
        Page<Order> findByUserIdAndStatusOrderByOrderDateDesc(@Param("userId") Integer userId, @Param("status") OrderStatus status, Pageable pageable);

        // Find order by ID and user ID (for security)
        Optional<Order> findByOrderIdAndUserId(Integer orderId, Integer userId);

        // Find order by ID and user ID with order details loaded
        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.orderId = :orderId AND o.userId = :userId")
        Optional<Order> findByOrderIdAndUserIdWithDetails(@Param("orderId") Integer orderId, @Param("userId") Integer userId);

        // Admin queries - find all orders with filters
        @Query("SELECT o FROM Order o WHERE " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(:userId IS NULL OR o.userId = :userId) AND " +
                        "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
                        "(:endDate IS NULL OR o.orderDate <= :endDate) " +
                        "ORDER BY o.orderDate DESC")
        Page<Order> findByFilters(@Param("status") OrderStatus status,
                        @Param("userId") Integer userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // Count orders by status
        long countByStatus(OrderStatus status);

        // Count orders by user ID
        long countByUserId(Integer userId);
}