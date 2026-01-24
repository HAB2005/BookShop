package com.example.system_backend.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "payment")
public class Payment {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.INIT;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        COD, // Cash on Delivery
        FAKE, // Fake payment for testing
        MOMO, // MoMo wallet
        VNPAY, // VNPay
        PAYPAL // PayPal
    }

    public enum PaymentStatus {
        INIT, // Khởi tạo
        PENDING, // Đang chờ xử lý
        SUCCESS, // Thành công
        FAILED, // Thất bại
        CANCELLED // Đã hủy
    }

    /**
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCESS;
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    /**
     * Check if payment is failed or cancelled
     */
    public boolean isFailedOrCancelled() {
        return this.status == PaymentStatus.FAILED || this.status == PaymentStatus.CANCELLED;
    }

    /**
     * Mark payment as successful
     */
    public void markAsSuccessful(String transactionRef) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionRef = transactionRef;
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.transactionRef = reason;
    }

    /**
     * Mark payment as cancelled
     */
    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }
}