package com.example.system_backend.product.mapper;

import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.entity.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductCategoryMapper handles mapping operations between ProductCategory entities and DTOs.
 * Pure mapper - no business logic or repository access.
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryMapper {

    /**
     * Map ProductCategory entities to CategoryResponse DTOs
     *
     * @param productCategories list of ProductCategory entities
     * @return list of CategoryResponse DTOs
     */
    public List<CategoryResponse> mapToCategoryResponses(List<ProductCategory> productCategories) {
        return productCategories.stream()
                .map(pc -> CategoryResponse.builder()
                        .categoryId(pc.getCategoryId())
                        .name(pc.getCategory() != null ? pc.getCategory().getName() : null)
                        .slug(pc.getCategory() != null ? pc.getCategory().getSlug() : null)
                        .parentId(pc.getCategory() != null ? pc.getCategory().getParentId() : null)
                        .status(pc.getCategory() != null ? pc.getCategory().getStatus().name() : null)
                        .createdAt(pc.getCategory() != null ? pc.getCategory().getCreatedAt() : null)
                        .children(null) // Don't load children for list view
                        .build())
                .collect(Collectors.toList());
    }
}

