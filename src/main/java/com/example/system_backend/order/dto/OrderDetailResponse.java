package com.example.system_backend.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailResponse {
    private Integer orderDetailId;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}