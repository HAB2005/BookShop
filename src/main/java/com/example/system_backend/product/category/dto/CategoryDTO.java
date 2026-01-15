package com.example.system_backend.product.category.dto;

import com.example.system_backend.common.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Internal DTO for Category data transfer between layers.
 * Does NOT depend on Entity - uses mapper instead.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Integer categoryId;
    private String name;
    private String slug;
    private Integer parentId;
    private CategoryStatus status;
    private LocalDateTime createdAt;
}
