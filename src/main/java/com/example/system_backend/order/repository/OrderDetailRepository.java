package com.example.system_backend.order.repository;

import com.example.system_backend.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Find order details by order ID
    List<OrderDetail> findByOrderOrderId(Integer orderId);

    // Find order details by product ID (for analytics)
    List<OrderDetail> findByProductId(Integer productId);

    // Get total quantity sold for a product
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.productId = :productId")
    Long getTotalQuantitySoldByProduct(@Param("productId") Integer productId);
}