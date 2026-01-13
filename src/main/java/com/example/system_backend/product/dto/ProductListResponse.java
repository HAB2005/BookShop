package com.example.system_backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import com.example.system_backend.product.category.dto.CategoryResponse;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {

    private Integer productId;
    private String name;
    private BigDecimal price;
    private String status; // Cần cho admin management
    private List<CategoryResponse> categories;
}
