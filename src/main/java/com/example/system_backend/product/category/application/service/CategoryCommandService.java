package com.example.system_backend.product.category.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.product.category.dto.CreateCategoryRequest;
import com.example.system_backend.product.category.dto.UpdateCategoryRequest;
import com.example.system_backend.product.category.dto.UpdateCategoryStatusRequest;
import com.example.system_backend.product.category.entity.Category;
import com.example.system_backend.product.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles Category-related write operations only. Pure CQRS - no cross-domain
 * orchestration. Transaction management is handled at Facade level.
 */
@Service
@RequiredArgsConstructor
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setParentId(request.getParentId());
        category.setStatus(Category.Status.ACTIVE);

        return categoryRepository.save(category);
    }

    public Category updateCategory(Integer categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getSlug() != null) {
            category.setSlug(request.getSlug());
        }

        if (request.getParentId() != null) {
            category.setParentId(request.getParentId());
        }

        return categoryRepository.save(category);
    }

    public void updateCategoryStatus(Integer categoryId, UpdateCategoryStatusRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Direct enum usage - no conversion needed
        category.setStatus(request.getStatus());
        categoryRepository.save(category);
    }
}
