package com.example.system_backend.product.category.domain;

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
}
