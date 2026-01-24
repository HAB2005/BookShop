package com.example.system_backend.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHistoryResponse {
    private Integer historyId;
    private Integer stockId;
    private Integer productId;
    private String productName;
    private ChangeTypeDto changeType;
    private Integer quantity;
    private String reason;
    private LocalDateTime createdAt;
}