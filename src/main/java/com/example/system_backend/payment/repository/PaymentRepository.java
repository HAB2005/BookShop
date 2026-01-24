package com.example.system_backend.payment.repository;

import com.example.system_backend.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Find payment by order ID
     */
    Optional<Payment> findByOrderId(Integer orderId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);

    /**
     * Find payments by method
     */
    List<Payment> findByMethodOrderByCreatedAtDesc(Payment.PaymentMethod method);

    /**
     * Find payments by transaction reference
     */
    Optional<Payment> findByTransactionRef(String transactionRef);

    /**
     * Find payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Find successful payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findSuccessfulPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Get total successful payment amount
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal getTotalSuccessfulPaymentAmount();

    /**
     * Get total successful payment amount within date range
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSuccessfulPaymentAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Count payments by status
     */
    Long countByStatus(Payment.PaymentStatus status);

    /**
     * Find recent payments with pagination
     */
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}