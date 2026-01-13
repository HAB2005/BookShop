package com.example.system_backend.product.image.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.image.domain.ProductImageValidationService;
import com.example.system_backend.product.image.dto.ReorderImagesRequest;
import com.example.system_backend.product.image.dto.UpdateImageRequest;
import com.example.system_backend.product.image.entity.ProductImage;
import com.example.system_backend.product.image.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ProductImageCommandService handles all write operations for ProductImage
 * domain. Pure command service - focuses on data manipulation operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageCommandService {

    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;
    private final ProductImageQueryService productImageQueryService;
    private final ProductImageValidationService productImageValidationService;

    /**
     * Upload and save product image
     */
    @Transactional
    public ProductImage uploadImage(MultipartFile file, Integer productId, Boolean isPrimary, Integer sortOrder) {
        // Store file and get relative path
        String imageUrl = fileStorageService.storeFile(file, productId);

        // Determine sort order if not provided
        if (sortOrder == null) {
            sortOrder = productImageQueryService.getMaxSortOrder(productId) + 1;
        }

        // Domain validation
        productImageValidationService.validateForCreation(productId, imageUrl, sortOrder);

        // Create and save ProductImage entity
        ProductImage productImage = new ProductImage();
        productImage.setProductId(productId);
        productImage.setImageUrl(imageUrl);
        productImage.setIsPrimary(isPrimary != null ? isPrimary : false);
        productImage.setSortOrder(sortOrder);

        ProductImage savedImage = productImageRepository.save(productImage);

        log.debug("Image uploaded and saved: {}", savedImage.getImageId());
        return savedImage;
    }

    /**
     * Update image properties
     */
    @Transactional
    public ProductImage updateImage(Integer imageId, UpdateImageRequest request) {
        ProductImage image = productImageQueryService.getImageById(imageId);

        // Domain validation
        productImageValidationService.validateForUpdate(image, request.getSortOrder());

        // Update isPrimary
        if (request.getIsPrimary() != null) {
            if (request.getIsPrimary()) {
                productImageValidationService.validateCanSetAsPrimary(image);
                image.setAsPrimary();
            } else {
                image.unsetAsPrimary();
            }
        }

        // Update sort order
        if (request.getSortOrder() != null) {
            image.updateSortOrder(request.getSortOrder());
        }

        ProductImage updatedImage = productImageRepository.save(image);

        log.debug("Image updated: {}", imageId);
        return updatedImage;
    }

    /**
     * Delete image
     */
    @Transactional
    public void deleteImage(Integer imageId) {
        ProductImage image = productImageQueryService.getImageById(imageId);

        // Domain validation
        if (!productImageValidationService.canBeDeleted(image)) {
            throw new ValidationException("Image cannot be deleted", "IMAGE_CANNOT_BE_DELETED");
        }

        // Delete file from storage
        try {
            fileStorageService.deleteFile(image.getImageUrl());
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", image.getImageUrl(), e);
            // Continue with database deletion even if file deletion fails
        }

        // Update sort orders for remaining images
        productImageRepository.decrementSortOrdersAfter(image.getProductId(), image.getSortOrder());

        // Delete from database
        productImageRepository.delete(image);

        log.debug("Image deleted: {}", imageId);
    }

    /**
     * Reorder images for a product
     */
    @Transactional
    public void reorderImages(ReorderImagesRequest request) {
        List<Integer> imageIds = request.getImageOrders().stream()
                .map(ReorderImagesRequest.ImageOrderItem::getImageId)
                .toList();

        List<ProductImage> images = productImageQueryService.getImagesByIds(imageIds);

        if (images.size() != imageIds.size()) {
            throw new ValidationException("Some images not found", "IMAGES_NOT_FOUND");
        }

        // Domain validation: ensure all images belong to the product
        for (ProductImage image : images) {
            productImageValidationService.validateImageBelongsToProduct(image, request.getProductId());
        }

        // Update sort orders
        for (ReorderImagesRequest.ImageOrderItem orderItem : request.getImageOrders()) {
            ProductImage image = images.stream()
                    .filter(img -> img.getImageId().equals(orderItem.getImageId()))
                    .findFirst()
                    .orElseThrow(
                            () -> new ResourceNotFoundException("ProductImage", "imageId", orderItem.getImageId()));

            // Domain validation for sort order
            productImageValidationService.validateSortOrder(orderItem.getSortOrder());
            image.updateSortOrder(orderItem.getSortOrder());
        }

        productImageRepository.saveAll(images);

        log.debug("Images reordered for product: {}", request.getProductId());
    }

    /**
     * Set image as primary
     */
    @Transactional
    public ProductImage setPrimaryImage(Integer imageId) {
        ProductImage image = productImageQueryService.getImageById(imageId);

        // Domain validation
        productImageValidationService.validateCanSetAsPrimary(image);

        // Set this image as primary
        image.setAsPrimary();

        ProductImage updatedImage = productImageRepository.save(image);

        log.debug("Image set as primary: {}", imageId);
        return updatedImage;
    }

    /**
     * Unset all primary images for a product
     */
    @Transactional
    public void unsetAllPrimaryImages(Integer productId) {
        productImageRepository.unsetAllPrimaryByProductId(productId);
        log.debug("All primary images unset for product: {}", productId);
    }

    /**
     * Delete all images for a product
     */
    @Transactional
    public void deleteAllProductImages(Integer productId) {
        List<ProductImage> images = productImageQueryService.getProductImages(productId);

        // Delete files from storage
        for (ProductImage image : images) {
            try {
                fileStorageService.deleteFile(image.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: {}", image.getImageUrl(), e);
            }
        }

        // Delete from database
        productImageRepository.deleteByProductId(productId);

        log.debug("All images deleted for product: {}", productId);
    }

    /**
     * Increment sort orders from a specific order
     */
    @Transactional
    public void incrementSortOrdersFrom(Integer productId, Integer fromOrder) {
        productImageRepository.incrementSortOrdersFrom(productId, fromOrder);
    }

    /**
     * Decrement sort orders after a specific order
     */
    @Transactional
    public void decrementSortOrdersAfter(Integer productId, Integer fromOrder) {
        productImageRepository.decrementSortOrdersAfter(productId, fromOrder);
    }
}
