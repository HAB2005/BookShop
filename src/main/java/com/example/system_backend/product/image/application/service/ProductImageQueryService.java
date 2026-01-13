package com.example.system_backend.product.image.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.product.image.entity.ProductImage;
import com.example.system_backend.product.image.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ProductImageQueryService handles all read operations for ProductImage domain.
 * Pure query service - no business logic, just data retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageQueryService {

    private final ProductImageRepository productImageRepository;

    /**
     * Get all images for a product, ordered by sort order
     */
    public List<ProductImage> getProductImages(Integer productId) {
        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
    }

    /**
     * Get primary image for a product
     */
    public Optional<ProductImage> getPrimaryImage(Integer productId) {
        return productImageRepository.findByProductIdAndIsPrimaryTrue(productId);
    }

    /**
     * Get image by ID
     */
    public ProductImage getImageById(Integer imageId) {
        return productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "imageId", imageId));
    }

    /**
     * Get image count for a product
     */
    public long getImageCount(Integer productId) {
        return productImageRepository.countByProductId(productId);
    }

    /**
     * Check if product has images
     */
    public boolean hasImages(Integer productId) {
        return productImageRepository.existsByProductId(productId);
    }

    /**
     * Get images by product ID and sort order range
     */
    public List<ProductImage> getImagesBySortOrderRange(Integer productId, Integer minOrder, Integer maxOrder) {
        return productImageRepository.findByProductIdAndSortOrderBetweenOrderBySortOrderAsc(
                productId, minOrder, maxOrder);
    }

    /**
     * Get max sort order for a product
     */
    public Integer getMaxSortOrder(Integer productId) {
        return productImageRepository.getMaxSortOrderByProductId(productId);
    }

    /**
     * Check if image exists
     */
    public boolean imageExists(Integer imageId) {
        return productImageRepository.existsById(imageId);
    }

    /**
     * Get all images by IDs
     */
    public List<ProductImage> getImagesByIds(List<Integer> imageIds) {
        return productImageRepository.findAllById(imageIds);
    }
}
