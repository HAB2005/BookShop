package com.example.system_backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import com.example.system_backend.product.book.dto.BookInfoResponse;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Integer productId;
    private String name;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;
    private BookInfoResponse book;
}
