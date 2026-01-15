package com.example.system_backend.product.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.product.domain.ProductValidationService;
import com.example.system_backend.product.dto.CreateProductRequest;
import com.example.system_backend.product.dto.UpdateProductRequest;
import com.example.system_backend.product.dto.UpdateProductStatusRequest;
import com.example.system_backend.product.entity.Product;
import com.example.system_backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProductCommandService handles ONLY Product entity write operations.
 * Pure CQRS - no cross-domain orchestration.
 */
@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    private final ProductValidationService productValidationService;

    /**
     * Create a new product (Product entity only)
     */
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        // Validate using domain service
        productValidationService.validateProductName(request.getName());
        productValidationService.validateProductPrice(request.getPrice());

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStatus(Product.Status.ACTIVE);

        return productRepository.save(product);
    }

    /**
     * Update existing product (Product entity only)
     */
    @Transactional
    public Product updateProduct(Integer productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Validate and update using domain service
        if (request.getName() != null) {
            productValidationService.validateProductName(request.getName());
            product.setName(request.getName().trim());
        }
        if (request.getPrice() != null) {
            productValidationService.validateProductPrice(request.getPrice());
            product.setPrice(request.getPrice());
        }

        return productRepository.save(product);
    }

    /**
     * Update product status (soft delete)
     */
    @Transactional
    public void updateProductStatus(Integer productId, UpdateProductStatusRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Validate using domain service
        productValidationService.validateStatusChange(product, request.getStatus());

        // Apply status change
        Product.Status newStatus = Product.Status.valueOf(request.getStatus().toUpperCase());
        product.setStatus(newStatus);

        productRepository.save(product);
    }

    /**
     * Assign categories to product (using service)
     */
    @Transactional
    public void assignCategoriesToProduct(Integer productId, List<Integer> categoryIds) {
        productCategoryService.assignCategoriesToProduct(productId, categoryIds);
    }
}