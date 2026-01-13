package com.example.system_backend.product.category.application.facade;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.product.category.application.service.CategoryCommandService;
import com.example.system_backend.product.category.application.service.CategoryQueryService;
import com.example.system_backend.product.category.domain.CategoryValidationService;
import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.category.dto.CreateCategoryRequest;
import com.example.system_backend.product.category.dto.UpdateCategoryRequest;
import com.example.system_backend.product.category.dto.UpdateCategoryStatusRequest;
import com.example.system_backend.product.category.entity.Category;
import com.example.system_backend.product.category.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CategoryFacade orchestrates category operations and coordinates between
 * services. Focuses on orchestration and delegation, with validation delegated
 * to domain service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryFacade {

    private final CategoryQueryService categoryQueryService;
    private final CategoryCommandService categoryCommandService;
    private final CategoryValidationService categoryValidationService;
    private final CategoryMapper categoryMapper;

    // ==================== QUERY OPERATIONS ====================
    public PageResponse<CategoryResponse> getCategories(int page, int size, String sortBy, String sortDir,
            String name, Integer parentId, boolean includeInactive) {

        // Validate and normalize pagination parameters at Facade level
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 100);
        sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "name";
        sortDir = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? "desc" : "asc";

        // Create Pageable
        Sort sort = sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<Category> categoryPage = categoryQueryService.getCategoriesRaw(
                pageable, name, parentId, includeInactive);

        // ✅ FIX N+1: Batch load all children in 1 query
        List<Integer> categoryIds = categoryPage.getContent().stream()
                .map(Category::getCategoryId)
                .toList();

        List<Category> allChildren = categoryQueryService.getActiveChildrenByParentIds(categoryIds);

        // Group children by parent ID for efficient lookup
        Map<Integer, List<Category>> childrenMap = allChildren.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        return categoryPage.map(category -> {
            List<Category> children = childrenMap.getOrDefault(category.getCategoryId(), List.of());
            return categoryMapper.mapToResponseWithChildren(category, children);
        });
    }

    public CategoryResponse getCategoryDetail(Integer categoryId) {
        Category category = categoryQueryService.getCategoryById(categoryId);

        // Use true recursive mapping for complete tree
        return categoryMapper.mapToResponseWithFullTree(category);
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryQueryService.getCategoryBySlug(slug);

        // Use true recursive mapping for complete tree
        return categoryMapper.mapToResponseWithFullTree(category);
    }

    /**
     * Get all descendant category IDs for given category IDs. Used by
     * ProductFacade for category filtering.
     */
    public List<Integer> getAllDescendantCategoryIds(List<Integer> categoryIds) {
        return categoryQueryService.getAllDescendantCategoryIds(categoryIds);
    }

    // ==================== COMMAND OPERATIONS ====================
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Delegate validation to domain service
        categoryValidationService.validateCategoryCreation(request.getSlug(), request.getParentId());

        // Create category
        Category savedCategory = categoryCommandService.createCategory(request);

        // Return with full tree for detail view
        return categoryMapper.mapToResponseWithFullTree(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Integer categoryId, UpdateCategoryRequest request) {
        log.info("Updating category: id={}", categoryId);

        // Delegate validation to domain service
        categoryValidationService.validateCategoryUpdate(categoryId, request.getSlug(), request.getParentId());

        // Update category
        Category updatedCategory = categoryCommandService.updateCategory(categoryId, request);

        // Return with full tree for detail view
        return categoryMapper.mapToResponseWithFullTree(updatedCategory);
    }

    @Transactional
    public void updateCategoryStatus(Integer categoryId, UpdateCategoryStatusRequest request) {
        log.info("Updating category status: id={}, status={}", categoryId, request.getStatus());

        // Validate deactivation if needed
        if (request.getStatus() == Category.Status.INACTIVE) {
            categoryValidationService.validateCategoryDeactivation(categoryId);
        }

        categoryCommandService.updateCategoryStatus(categoryId, request);
        log.info("Category status updated successfully: id={}", categoryId);
    }
}
