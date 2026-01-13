package com.example.system_backend.product.mapper;

import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.entity.ProductCategory;
import com.example.system_backend.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductCategoryMapper handles mapping operations between Product and Category
 * entities. Separates mapping logic from business logic in services.
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryMapper {

    private final ProductCategoryRepository productCategoryRepository;

    /**
     * Assign categories to a product
     *
     * @param productId the product ID
     * @param categoryIds list of category IDs to assign
     */
    @Transactional
    public void assignCategoriesToProduct(Integer productId, List<Integer> categoryIds) {
        // Remove existing category assignments
        productCategoryRepository.deleteByProductId(productId);

        // Create new ProductCategory entries
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Integer categoryId : categoryIds) {
                ProductCategory productCategory = new ProductCategory();
                productCategory.setProductId(productId);
                productCategory.setCategoryId(categoryId);
                productCategoryRepository.save(productCategory);
            }
        }
    }

    /**
     * Add a single category to a product (without removing existing ones)
     *
     * @param productId the product ID
     * @param categoryId the category ID to add
     */
    @Transactional
    public void addCategoryToProduct(Integer productId, Integer categoryId) {
        // Check if mapping already exists by trying to find it
        List<ProductCategory> existing = productCategoryRepository.findByProductId(productId);
        boolean alreadyExists = existing.stream()
                .anyMatch(pc -> pc.getCategoryId().equals(categoryId));

        if (!alreadyExists) {
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductId(productId);
            productCategory.setCategoryId(categoryId);
            productCategoryRepository.save(productCategory);
        }
    }

    /**
     * Get categories for a product as CategoryResponse DTOs
     *
     * @param productId the product ID
     * @return list of CategoryResponse DTOs
     */
    public List<CategoryResponse> getCategoriesForProduct(Integer productId) {
        List<ProductCategory> productCategories = productCategoryRepository.findByProductId(productId);
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
