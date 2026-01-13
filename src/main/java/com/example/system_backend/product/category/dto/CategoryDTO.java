package com.example.system_backend.product.category.dto;

import com.example.system_backend.product.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Internal DTO for Category data transfer between layers. Prevents entity
 * leakage from domain to application layer.
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
    private Category.Status status;
    private LocalDateTime createdAt;

    /**
     * Convert from Entity to DTO
     */
    public static CategoryDTO from(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .build();
    }

    /**
     * Convert to Entity (for cases where needed)
     */
    public Category toEntity() {
        Category category = new Category();
        category.setCategoryId(this.categoryId);
        category.setName(this.name);
        category.setSlug(this.slug);
        category.setParentId(this.parentId);
        category.setStatus(this.status);
        category.setCreatedAt(this.createdAt);
        return category;
    }
}
