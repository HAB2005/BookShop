package com.example.system_backend.order.dto;

import lombok.Data;

@Data
public class OrderStatisticsResponse {
    private long pendingCount;
    private long processingCount;
    private long shippedCount;
    private long deliveredCount;
    private long cancelledCount;
    private long totalCount;
}