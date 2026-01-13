package com.example.system_backend.product.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private Integer imageId;
    private Integer productId;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}