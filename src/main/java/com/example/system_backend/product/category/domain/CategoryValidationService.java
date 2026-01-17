package com.example.system_backend.product.category.domain;

import com.example.system_backend.common.enums.CategoryStatus;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.product.category.entity.Category;
import com.example.system_backend.product.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain service for category validation rules. Encapsulates all business
 * validation logic for categories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryValidationService {

    private final CategoryRepository categoryRepository;

    /**
     * Validates slug uniqueness for new category creation.
     *
     * @param slug the slug to validate (must not be null or empty)
     * @throws ValidationException if slug is invalid or already exists
     */
    public void validateSlugUniqueness(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new ValidationException(
                    CategoryValidationError.SLUG_NULL_OR_EMPTY.getMessage(),
                    CategoryValidationError.SLUG_NULL_OR_EMPTY.getCode());
        }

        if (categoryRepository.existsBySlug(slug)) {
            throw new ValidationException(
                    CategoryValidationError.SLUG_EXISTS.getMessage(),
                    CategoryValidationError.SLUG_EXISTS.getCode());
        }
    }

    /**
     * Validates slug uniqueness for category update (excluding current
     * category).
     *
     * @param slug       the slug to validate (must not be null or empty)
     * @param categoryId the current category ID to exclude (must not be null)
     * @throws ValidationException if parameters are invalid or slug already
     *                             exists
     */
    public void validateSlugUniquenessForUpdate(String slug, Integer categoryId) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new ValidationException(
                    CategoryValidationError.SLUG_NULL_OR_EMPTY.getMessage(),
                    CategoryValidationError.SLUG_NULL_OR_EMPTY.getCode());
        }
        if (categoryId == null) {
            throw new ValidationException(
                    CategoryValidationError.CATEGORY_ID_NULL.getMessage(),
                    CategoryValidationError.CATEGORY_ID_NULL.getCode());
        }

        if (categoryRepository.existsBySlugAndCategoryIdNot(slug, categoryId)) {
            throw new ValidationException(
                    CategoryValidationError.SLUG_EXISTS.getMessage(),
                    CategoryValidationError.SLUG_EXISTS.getCode());
        }
    }

    /**
     * Validates parent category business rules.
     *
     * @param parentId the parent category ID to validate (must not be null)
     * @throws ValidationException if parentId is null or parent category is
     *                             invalid
     */
    public void validateParentCategory(Integer parentId) {
        if (parentId == null) {
            throw new ValidationException(
                    CategoryValidationError.PARENT_ID_NULL.getMessage(),
                    CategoryValidationError.PARENT_ID_NULL.getCode());
        }

        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));

        if (parentCategory.getStatus() != CategoryStatus.ACTIVE) {
            throw new ValidationException(
                    CategoryValidationError.PARENT_INACTIVE.getMessage(),
                    CategoryValidationError.PARENT_INACTIVE.getCode());
        }
    }

    /**
     * Validates circular reference prevention.
     *
     * @param categoryId the category ID being updated (must not be null)
     * @param parentId   the new parent ID (must not be null)
     * @throws ValidationException if any parameter is null or circular
     *                             reference is detected
     */
    public void validateCircularReference(Integer categoryId, Integer parentId) {
        if (categoryId == null) {
            throw new ValidationException(
                    CategoryValidationError.CATEGORY_ID_NULL.getMessage(),
                    CategoryValidationError.CATEGORY_ID_NULL.getCode());
        }
        if (parentId == null) {
            throw new ValidationException(
                    CategoryValidationError.PARENT_ID_NULL.getMessage(),
                    CategoryValidationError.PARENT_ID_NULL.getCode());
        }

        // Prevent self-reference
        if (parentId.equals(categoryId)) {
            throw new ValidationException(
                    CategoryValidationError.SELF_REFERENCE.getMessage(),
                    CategoryValidationError.SELF_REFERENCE.getCode());
        }

        // Prevent descendant as parent
        List<Integer> descendants = getAllDescendantCategoryIds(List.of(categoryId));
        if (descendants.contains(parentId)) {
            throw new ValidationException(
                    CategoryValidationError.CIRCULAR_REFERENCE.getMessage(),
                    CategoryValidationError.CIRCULAR_REFERENCE.getCode());
        }
    }

    /**
     * Validates category deactivation rules.
     *
     * @param categoryId the category ID to validate (must not be null)
     * @throws ValidationException if categoryId is null or category has active
     *                             children
     */
    public void validateCategoryDeactivation(Integer categoryId) {
        if (categoryId == null) {
            throw new ValidationException(
                    CategoryValidationError.CATEGORY_ID_NULL.getMessage(),
                    CategoryValidationError.CATEGORY_ID_NULL.getCode());
        }

        if (hasActiveChildren(categoryId)) {
            throw new ValidationException(
                    CategoryValidationError.HAS_ACTIVE_CHILDREN.getMessage(),
                    CategoryValidationError.HAS_ACTIVE_CHILDREN.getCode());
        }
    }

    /**
     * Check if category has active children
     */
    private boolean hasActiveChildren(Integer categoryId) {
        return !categoryRepository.findByParentIdAndStatus(categoryId, CategoryStatus.ACTIVE).isEmpty();
    }

    /**
     * Get all descendant category IDs for given category IDs (including
     * themselves).
     * Handles recursive traversal of the category tree.
     */
    private List<Integer> getAllDescendantCategoryIds(List<Integer> categoryIds) {
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
     * Java-based recursive method to get all descendant category IDs.
     * Fallback for databases that don't support Common Table Expressions (CTE).
     */
    private List<Integer> getAllDescendantCategoryIdsRecursive(List<Integer> categoryIds) {
        Set<Integer> allCategoryIds = new HashSet<>(categoryIds);
        List<Integer> currentLevelIds = new ArrayList<>(categoryIds);

        while (!currentLevelIds.isEmpty()) {
            List<Category> children = categoryRepository.findByParentIdInAndStatus(
                    currentLevelIds, CategoryStatus.ACTIVE);

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

    /**
     * Comprehensive validation for category creation.
     */
    public void validateCategoryCreation(String slug, Integer parentId) {
        validateSlugUniqueness(slug);

        if (parentId != null) {
            validateParentCategory(parentId);
        }
    }

    /**
     * Comprehensive validation for category update.
     */
    public void validateCategoryUpdate(Integer categoryId, String slug, Integer parentId) {
        if (slug != null) {
            validateSlugUniquenessForUpdate(slug, categoryId);
        }

        if (parentId != null) {
            validateParentCategory(parentId);
            validateCircularReference(categoryId, parentId);
        }
    }
}
