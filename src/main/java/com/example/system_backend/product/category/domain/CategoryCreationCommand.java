package com.example.system_backend.product.category.domain;

import com.example.system_backend.product.category.dto.CreateCategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain command for category creation. Isolates domain layer from application
 * DTOs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreationCommand {

    private String name;
    private String slug;
    private Integer parentId;

    /**
     * Create command from application request DTO
     */
    public static CategoryCreationCommand from(CreateCategoryRequest request) {
        return CategoryCreationCommand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .parentId(request.getParentId())
                .build();
    }
}
