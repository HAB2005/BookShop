package com.example.system_backend.product.category.mapper;

import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.category.entity.Category;
import com.example.system_backend.product.category.application.service.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps Category entities to CategoryResponse DTOs.
 * Handles complex mapping logic including recursive children mapping.
 */
@Component
@RequiredArgsConstructor
public class CategoryMapper {

        private final CategoryQueryService categoryQueryService;

        /**
         * Map Category entity to CategoryResponse DTO with full recursive children
         * tree.
         * This method performs true recursion to build complete category tree.
         */
        public CategoryResponse mapToResponseWithFullTree(Category category) {
                // Get direct children
                List<Category> children = categoryQueryService.getCategoryChildren(category.getCategoryId());

                return CategoryResponse.builder()
                                .categoryId(category.getCategoryId())
                                .name(category.getName())
                                .slug(category.getSlug())
                                .parentId(category.getParentId())
                                .status(category.getStatus().name())
                                .createdAt(category.getCreatedAt())
                                .children(children.stream()
                                                .map(this::mapToResponseWithFullTree) // ✅ TRUE RECURSION
                                                .collect(Collectors.toList()))
                                .build();
        }

        /**
         * Map Category entity to CategoryResponse DTO with provided children (1 level
         * only).
         * Used when children are already fetched to avoid N+1 queries.
         */
        public CategoryResponse mapToResponseWithChildren(Category category, List<Category> children) {
                return CategoryResponse.builder()
                                .categoryId(category.getCategoryId())
                                .name(category.getName())
                                .slug(category.getSlug())
                                .parentId(category.getParentId())
                                .status(category.getStatus().name())
                                .createdAt(category.getCreatedAt())
                                .children(children.stream()
                                                .map(this::mapToResponse) // ✅ Only 1 level, no false promises
                                                .collect(Collectors.toList()))
                                .build();
        }

        /**
         * Map Category entity to CategoryResponse DTO without children.
         * Used for simple mapping scenarios.
         */
        public CategoryResponse mapToResponse(Category category) {
                return CategoryResponse.builder()
                                .categoryId(category.getCategoryId())
                                .name(category.getName())
                                .slug(category.getSlug())
                                .parentId(category.getParentId())
                                .status(category.getStatus().name())
                                .createdAt(category.getCreatedAt())
                                .children(List.of()) // Empty children list
                                .build();
        }

        /**
         * Map list of Category entities to CategoryResponse DTOs without children.
         * Used for list responses where children are not needed.
         */
        public List<CategoryResponse> mapToResponseList(List<Category> categories) {
                return categories.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Map Category entity to CategoryResponse DTO with optimized tree loading.
         * Loads all descendants in batch to avoid N+1 queries.
         */
        public CategoryResponse mapToResponseWithOptimizedTree(Category rootCategory) {
                // Get all descendants in one query
                List<Integer> allDescendantIds = categoryQueryService.getAllDescendantCategoryIds(
                                List.of(rootCategory.getCategoryId()));

                // Fetch all categories in batch
                List<Category> allCategories = categoryQueryService.getCategoriesByIds(allDescendantIds);

                // Build tree structure
                return buildTreeFromFlatList(rootCategory, allCategories);
        }

        /**
         * Build tree structure from flat list of categories.
         * Efficient tree building without recursive queries.
         */
        private CategoryResponse buildTreeFromFlatList(Category rootCategory, List<Category> allCategories) {
                // Create a map for quick lookup
                Map<Integer, List<Category>> childrenMap = allCategories.stream()
                                .filter(cat -> cat.getParentId() != null)
                                .collect(Collectors.groupingBy(Category::getParentId));

                return buildCategoryResponse(rootCategory, childrenMap);
        }

        private CategoryResponse buildCategoryResponse(Category category, Map<Integer, List<Category>> childrenMap) {
                List<Category> children = childrenMap.getOrDefault(category.getCategoryId(), List.of());

                return CategoryResponse.builder()
                                .categoryId(category.getCategoryId())
                                .name(category.getName())
                                .slug(category.getSlug())
                                .parentId(category.getParentId())
                                .status(category.getStatus().name())
                                .createdAt(category.getCreatedAt())
                                .children(children.stream()
                                                .map(child -> buildCategoryResponse(child, childrenMap))
                                                .collect(Collectors.toList()))
                                .build();
        }
}