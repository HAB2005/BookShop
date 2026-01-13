package com.example.system_backend.product.category.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.product.category.entity.Category;
import com.example.system_backend.product.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles Category-related read operations only. Pure CQRS - no cross-domain
 * orchestration.
 */
@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public PageResponse<Category> getCategoriesRaw(Pageable pageable, String name, Integer parentId,
            boolean includeInactive) {

        // Query categories based on includeInactive flag
        Page<Category> categoryPage;
        if (includeInactive) {
            categoryPage = categoryRepository.findAllCategoriesWithFilters(name, parentId, pageable);
        } else {
            categoryPage = categoryRepository.findCategoriesWithFilters(
                    name, Category.Status.ACTIVE, parentId, pageable);
        }

        return PageResponse.<Category>builder()
                .content(categoryPage.getContent())
                .page(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .first(categoryPage.isFirst())
                .last(categoryPage.isLast())
                .empty(categoryPage.isEmpty())
                .build();
    }

    public Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
    }

    public List<Category> getCategoryChildren(Integer categoryId) {
        return categoryRepository.findByParentIdAndStatus(categoryId, Category.Status.ACTIVE);
    }

    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    public boolean existsBySlugExcludingId(String slug, Integer excludeId) {
        return categoryRepository.existsBySlugAndCategoryIdNot(slug, excludeId);
    }

    /**
     * Get all descendant category IDs for given category IDs (including
     * themselves). Handles recursive traversal of the category tree.
     */
    public List<Integer> getAllDescendantCategoryIds(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Try using native recursive query first
            return categoryRepository.findAllDescendantCategoryIds(categoryIds);
        } catch (Exception e) {
            // Fallback to Java-based recursive approach
            return getAllDescendantCategoryIdsRecursive(categoryIds);
        }
    }

    /**
     * Java-based recursive method to get all descendant category IDs. Fallback
     * for databases that don't support Common Table Expressions (CTE).
     */
    private List<Integer> getAllDescendantCategoryIdsRecursive(List<Integer> categoryIds) {
        Set<Integer> allCategoryIds = new HashSet<>(categoryIds);
        List<Integer> currentLevelIds = new ArrayList<>(categoryIds);

        while (!currentLevelIds.isEmpty()) {
            List<Category> children = categoryRepository.findByParentIdInAndStatus(
                    currentLevelIds, Category.Status.ACTIVE);

            List<Integer> childIds = children.stream()
                    .map(Category::getCategoryId)
                    .filter(id -> !allCategoryIds.contains(id))
                    .collect(Collectors.toList());

            if (childIds.isEmpty()) {
                break;
            }

            allCategoryIds.addAll(childIds);
            currentLevelIds = childIds;
        }

        return new ArrayList<>(allCategoryIds);
    }

    public List<Category> getActiveChildrenByParentIds(List<Integer> parentIds) {
        return categoryRepository.findByParentIdInAndStatus(parentIds, Category.Status.ACTIVE);
    }

    public boolean hasActiveChildren(Integer categoryId) {
        return !categoryRepository.findByParentIdAndStatus(categoryId, Category.Status.ACTIVE).isEmpty();
    }

    public List<Category> getCategoriesByIds(List<Integer> categoryIds) {
        return categoryRepository.findAllById(categoryIds);
    }
}
