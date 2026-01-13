package com.example.system_backend.product.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(max = 100, message = "Category slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    private Integer parentId; // Optional - null for root categories
}
