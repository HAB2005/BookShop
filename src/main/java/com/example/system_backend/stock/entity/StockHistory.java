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
@Table(name = "stock_history")
public class StockHistory {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "stock_id", nullable = false)
    private Integer stockId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 100)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ChangeType {
        IN, // Nhập kho
        OUT, // Xuất kho
        ADJUST // Điều chỉnh
    }

    // Constructor for easy creation
    public StockHistory(Integer stockId, ChangeType changeType, Integer quantity, String reason) {
        this.stockId = stockId;
        this.changeType = changeType;
        this.quantity = quantity;
        this.reason = reason;
    }
}