package com.example.system_backend.product.category.domain;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.category.application.service.CategoryQueryService;
import com.example.system_backend.product.category.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain service for category validation rules. Encapsulates all business
 * validation logic for categories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryValidationService {

    private final CategoryQueryService categoryQueryService;

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

        if (categoryQueryService.existsBySlug(slug)) {
            throw new ValidationException(
                    CategoryValidationError.SLUG_EXISTS.getMessage(),
                    CategoryValidationError.SLUG_EXISTS.getCode());
        }
    }

    /**
     * Validates slug uniqueness for category update (excluding current
     * category).
     *
     * @param slug the slug to validate (must not be null or empty)
     * @param categoryId the current category ID to exclude (must not be null)
     * @throws ValidationException if parameters are invalid or slug already
     * exists
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

        if (categoryQueryService.existsBySlugExcludingId(slug, categoryId)) {
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
     * invalid
     */
    public void validateParentCategory(Integer parentId) {
        if (parentId == null) {
            throw new ValidationException(
                    CategoryValidationError.PARENT_ID_NULL.getMessage(),
                    CategoryValidationError.PARENT_ID_NULL.getCode());
        }

        Category parentCategory = categoryQueryService.getCategoryById(parentId);

        if (parentCategory.getStatus() != Category.Status.ACTIVE) {
            throw new ValidationException(
                    CategoryValidationError.PARENT_INACTIVE.getMessage(),
                    CategoryValidationError.PARENT_INACTIVE.getCode());
        }
    }

    /**
     * Validates circular reference prevention.
     *
     * @param categoryId the category ID being updated (must not be null)
     * @param parentId the new parent ID (must not be null)
     * @throws ValidationException if any parameter is null or circular
     * reference is detected
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
        List<Integer> descendants = categoryQueryService.getAllDescendantCategoryIds(List.of(categoryId));
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
     * children
     */
    public void validateCategoryDeactivation(Integer categoryId) {
        if (categoryId == null) {
            throw new ValidationException(
                    CategoryValidationError.CATEGORY_ID_NULL.getMessage(),
                    CategoryValidationError.CATEGORY_ID_NULL.getCode());
        }

        if (categoryQueryService.hasActiveChildren(categoryId)) {
            throw new ValidationException(
                    CategoryValidationError.HAS_ACTIVE_CHILDREN.getMessage(),
                    CategoryValidationError.HAS_ACTIVE_CHILDREN.getCode());
        }
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
