package com.example.system_backend.product.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private Integer categoryId;
    private String name;
    private String slug;
    private Integer parentId;
    private String status;
    private LocalDateTime createdAt;
    private List<CategoryResponse> children;
}