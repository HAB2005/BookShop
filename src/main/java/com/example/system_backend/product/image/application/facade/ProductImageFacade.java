package com.example.system_backend.product.image.application.facade;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.application.service.ProductQueryService;
import com.example.system_backend.product.entity.Product;
import com.example.system_backend.product.image.application.service.ProductImageCommandService;
import com.example.system_backend.product.image.application.service.ProductImageQueryService;
import com.example.system_backend.product.image.dto.ProductImageResponse;
import com.example.system_backend.product.image.dto.ReorderImagesRequest;
import com.example.system_backend.product.image.dto.UpdateImageRequest;
import com.example.system_backend.product.image.entity.ProductImage;
import com.example.system_backend.product.image.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * ProductImageFacade orchestrates cross-domain operations between Product and
 * ProductImage domains. Handles complex image management workflows and
 * coordinates multiple services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageFacade {

    // Domain services
    private final ProductImageCommandService productImageCommandService;
    private final ProductImageQueryService productImageQueryService;
    private final ProductQueryService productQueryService;
    private final ProductImageMapper productImageMapper;

    // Business rules constants
    private static final int MAX_IMAGES_PER_PRODUCT = 10;

    /**
     * Upload and save product image with cross-domain validation
     */
    @Transactional
    public ProductImageResponse uploadImage(MultipartFile file, Integer productId, Boolean isPrimary, Integer sortOrder) {
        // Cross-domain validation: ensure product exists and is active
        validateProductExists(productId);

        // Business rule: check image limit
        validateImageLimit(productId);

        // Business logic: handle primary image logic
        if (isPrimary != null && isPrimary) {
            productImageCommandService.unsetAllPrimaryImages(productId);
        }

        // Delegate to command service
        ProductImage savedImage = productImageCommandService.uploadImage(file, productId, isPrimary, sortOrder);

        log.info("Image uploaded successfully for product {}: {}", productId, savedImage.getImageUrl());
        return productImageMapper.mapToResponse(savedImage);
    }

    /**
     * Get all images for a product with product validation
     */
    public List<ProductImageResponse> getProductImages(Integer productId) {
        // Cross-domain validation: ensure product exists
        validateProductExists(productId);

        List<ProductImage> images = productImageQueryService.getProductImages(productId);
        return productImageMapper.mapToResponseList(images);
    }

    /**
     * Get primary image for a product
     */
    public Optional<ProductImageResponse> getPrimaryImage(Integer productId) {
        // Cross-domain validation: ensure product exists
        validateProductExists(productId);

        return productImageQueryService.getPrimaryImage(productId)
                .map(productImageMapper::mapToResponse);
    }

    /**
     * Get image by ID with validation
     */
    public ProductImageResponse getImageById(Integer imageId) {
        ProductImage image = productImageQueryService.getImageById(imageId);
        return productImageMapper.mapToResponse(image);
    }

    /**
     * Update image properties with business logic
     */
    @Transactional
    public ProductImageResponse updateImage(Integer imageId, UpdateImageRequest request) {
        ProductImage image = productImageQueryService.getImageById(imageId);

        // Business logic: handle primary image logic
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            productImageCommandService.unsetAllPrimaryImages(image.getProductId());
        }

        ProductImage updatedImage = productImageCommandService.updateImage(imageId, request);
        return productImageMapper.mapToResponse(updatedImage);
    }

    /**
     * Delete image with cleanup
     */
    @Transactional
    public void deleteImage(Integer imageId) {
        ProductImage image = productImageQueryService.getImageById(imageId);

        // Business logic: if deleting primary image, set another as primary
        if (image.isPrimary()) {
            handlePrimaryImageDeletion(image.getProductId(), imageId);
        }

        productImageCommandService.deleteImage(imageId);

        log.info("Image deleted successfully: {}", imageId);
    }

    /**
     * Reorder images with validation
     */
    @Transactional
    public void reorderImages(ReorderImagesRequest request) {
        Integer productId = request.getProductId();

        // Cross-domain validation: ensure product exists
        validateProductExists(productId);

        // Business validation: ensure all images belong to the product
        validateImagesOwnership(request);

        productImageCommandService.reorderImages(request);

        log.info("Images reordered successfully for product: {}", productId);
    }

    /**
     * Set image as primary with business logic
     */
    @Transactional
    public ProductImageResponse setPrimaryImage(Integer imageId) {
        ProductImage image = productImageQueryService.getImageById(imageId);

        // Unset all other primary images for this product
        productImageCommandService.unsetAllPrimaryImages(image.getProductId());

        ProductImage updatedImage = productImageCommandService.setPrimaryImage(imageId);
        return productImageMapper.mapToResponse(updatedImage);
    }

    /**
     * Delete all images for a product
     */
    @Transactional
    public void deleteAllProductImages(Integer productId) {
        // Cross-domain validation: ensure product exists
        validateProductExists(productId);

        productImageCommandService.deleteAllProductImages(productId);

        log.info("All images deleted for product: {}", productId);
    }

    /**
     * Get image count for a product
     */
    public long getImageCount(Integer productId) {
        // Cross-domain validation: ensure product exists
        validateProductExists(productId);

        return productImageQueryService.getImageCount(productId);
    }

    /**
     * Check if product has images
     */
    public boolean hasImages(Integer productId) {
        // Cross-domain validation: ensure product exists
        validateProductExists(productId);

        return productImageQueryService.hasImages(productId);
    }

    // ===== PRIVATE HELPER METHODS (Business Logic) =====
    /**
     * Cross-domain validation: ensure product exists and is active
     */
    private void validateProductExists(Integer productId) {
        try {
            Product product = productQueryService.getProductById(productId);
            if (product.getStatus() == Product.Status.DELETED) {
                throw new ValidationException("Cannot manage images for deleted product", "PRODUCT_DELETED");
            }
        } catch (ResourceNotFoundException e) {
            throw new ValidationException("Product not found: " + productId, "PRODUCT_NOT_FOUND");
        }
    }

    /**
     * Business rule: validate image limit per product
     */
    private void validateImageLimit(Integer productId) {
        long currentCount = productImageQueryService.getImageCount(productId);
        if (currentCount >= MAX_IMAGES_PER_PRODUCT) {
            throw new ValidationException(
                    String.format("Product can have maximum %d images", MAX_IMAGES_PER_PRODUCT),
                    "IMAGE_LIMIT_EXCEEDED");
        }
    }

    /**
     * Business validation: ensure all images belong to the product
     */
    private void validateImagesOwnership(ReorderImagesRequest request) {
        List<Integer> imageIds = request.getImageOrders().stream()
                .map(ReorderImagesRequest.ImageOrderItem::getImageId)
                .toList();

        for (Integer imageId : imageIds) {
            ProductImage image = productImageQueryService.getImageById(imageId);
            if (!image.getProductId().equals(request.getProductId())) {
                throw new ValidationException(
                        "Image " + imageId + " does not belong to product " + request.getProductId(),
                        "INVALID_IMAGE_OWNERSHIP");
            }
        }
    }

    /**
     * Business logic: handle primary image deletion
     */
    private void handlePrimaryImageDeletion(Integer productId, Integer deletingImageId) {
        List<ProductImage> otherImages = productImageQueryService.getProductImages(productId)
                .stream()
                .filter(img -> !img.getImageId().equals(deletingImageId))
                .toList();

        if (!otherImages.isEmpty()) {
            // Set the first remaining image as primary
            ProductImage newPrimary = otherImages.get(0);
            productImageCommandService.setPrimaryImage(newPrimary.getImageId());
            log.info("Set image {} as new primary after deleting primary image {}",
                    newPrimary.getImageId(), deletingImageId);
        }
    }
}
