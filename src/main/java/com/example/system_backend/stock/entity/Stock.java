package com.example.system_backend.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(name = "uk_stock_product", columnNames = {"product_id"}))
public class Stock {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    @Column(name = "product_id", nullable = false, unique = true)
    private Integer productId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 5;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Check if enough stock available
     */
    public boolean hasStock(Integer quantity) {
        return this.availableQuantity >= quantity;
    }

    /**
     * Check if stock is low
     */
    public boolean isLowStock() {
        return this.availableQuantity <= this.lowStockThreshold;
    }

    /**
     * Reduce stock (when payment successful)
     */
    public void reduceStock(Integer quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock available");
        }
        this.availableQuantity -= quantity;
    }

    /**
     * Add stock (restock)
     */
    public void addStock(Integer quantity) {
        this.availableQuantity += quantity;
    }

    /**
     * Set stock quantity
     */
    public void setStock(Integer quantity) {
        this.availableQuantity = quantity;
    }
}