package com.example.system_backend.product.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.product.entity.Product;
import com.example.system_backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductQueryService handles ONLY Product entity read operations.
 * Pure CQRS - no cross-domain orchestration.
 */
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    /**
     * Get paginated list of products with filters (Product entity only)
     */
    public PageResponse<Product> getProductsRaw(int page, int size, String sortBy, String sortDir,
            String name, BigDecimal minPrice, BigDecimal maxPrice, List<Integer> categoryIds,
            boolean includeAllStatuses) {

        // Validate and set default values
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 100);
        sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "createdAt";
        sortDir = (sortDir != null && sortDir.equalsIgnoreCase("asc")) ? "asc" : "desc";

        // Create sort object
        Sort sort = sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Query products with filters
        Page<Product> productPage;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (includeAllStatuses) {
                productPage = productRepository.findProductsWithCategoryFiltersAllStatuses(
                        name, minPrice, maxPrice, categoryIds, pageable);
            } else {
                productPage = productRepository.findProductsWithCategoryFilters(
                        name, Product.Status.ACTIVE, minPrice, maxPrice, categoryIds, pageable);
            }
        } else {
            if (includeAllStatuses) {
                productPage = productRepository.findProductsWithFiltersAllStatuses(
                        name, minPrice, maxPrice, pageable);
            } else {
                productPage = productRepository.findProductsWithFilters(
                        name, Product.Status.ACTIVE, minPrice, maxPrice, pageable);
            }
        }

        return PageResponse.<Product>builder()
                .content(productPage.getContent())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    /**
     * Overloaded method for backward compatibility
     */
    public PageResponse<Product> getProductsRaw(int page, int size, String sortBy, String sortDir,
            String name, BigDecimal minPrice, BigDecimal maxPrice, List<Integer> categoryIds) {
        return getProductsRaw(page, size, sortBy, sortDir, name, minPrice, maxPrice, categoryIds, false);
    }

    /**
     * Get single product by ID (Product entity only)
     */
    public Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }
}