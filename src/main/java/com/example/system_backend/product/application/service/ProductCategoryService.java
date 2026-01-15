package com.example.system_backend.product.application.service;

import com.example.system_backend.product.entity.ProductCategory;
import com.example.system_backend.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing product-category relationships.
 * Handles the business logic for assigning categories to products.
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryService {

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
        // Check if mapping already exists
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
     * Get product categories
     *
     * @param productId the product ID
     * @return list of ProductCategory entities
     */
    public List<ProductCategory> getProductCategories(Integer productId) {
        return productCategoryRepository.findByProductId(productId);
    }
}
