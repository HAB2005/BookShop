package com.example.system_backend.product.image.domain;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.image.entity.ProductImage;
import org.springframework.stereotype.Service;

/**
 * ProductImageValidationService contains domain validation logic for
 * ProductImage. Pure domain service - no external dependencies, only business
 * rules.
 */
@Service
public class ProductImageValidationService {

    /**
     * Validate if image can be set as primary
     */
    public void validateCanSetAsPrimary(ProductImage image) {
        if (image == null) {
            throw new ValidationException("Image cannot be null", "IMAGE_NULL");
        }

        if (image.getProductId() == null) {
            throw new ValidationException("Image must belong to a product", "PRODUCT_ID_NULL");
        }
    }

    /**
     * Validate sort order value
     */
    public void validateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new ValidationException("Sort order must be non-negative", "INVALID_SORT_ORDER");
        }
    }

    /**
     * Validate image URL
     */
    public void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new ValidationException("Image URL cannot be empty", "IMAGE_URL_EMPTY");
        }

        // Basic URL format validation
        if (!imageUrl.matches("^[a-zA-Z0-9/._-]+\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new ValidationException("Invalid image URL format", "INVALID_IMAGE_URL_FORMAT");
        }
    }

    /**
     * Validate product ID
     */
    public void validateProductId(Integer productId) {
        if (productId == null || productId <= 0) {
            throw new ValidationException("Product ID must be positive", "INVALID_PRODUCT_ID");
        }
    }

    /**
     * Domain rule: Check if image is primary
     */
    public boolean isPrimary(ProductImage image) {
        return image.getIsPrimary() != null && image.getIsPrimary();
    }

    /**
     * Domain rule: Check if image can be deleted
     */
    public boolean canBeDeleted(ProductImage image) {
        // Business rule: Any image can be deleted
        // Future: might add restrictions based on business requirements
        return image != null && image.getImageId() != null;
    }

    /**
     * Domain rule: Check if sort order is valid for update
     */
    public boolean isValidSortOrderUpdate(Integer currentOrder, Integer newOrder) {
        if (newOrder == null) {
            return false;
        }

        if (newOrder < 0) {
            return false;
        }

        // Allow same order (no change)
        return true;
    }

    /**
     * Domain rule: Validate image belongs to product
     */
    public void validateImageBelongsToProduct(ProductImage image, Integer expectedProductId) {
        if (image == null) {
            throw new ValidationException("Image cannot be null", "IMAGE_NULL");
        }

        if (!image.getProductId().equals(expectedProductId)) {
            throw new ValidationException(
                    String.format("Image %d does not belong to product %d",
                            image.getImageId(), expectedProductId),
                    "IMAGE_PRODUCT_MISMATCH");
        }
    }

    /**
     * Domain rule: Check if image URL format is supported
     */
    public boolean isSupportedImageFormat(String imageUrl) {
        if (imageUrl == null) {
            return false;
        }

        String lowerUrl = imageUrl.toLowerCase();
        return lowerUrl.endsWith(".jpg")
                || lowerUrl.endsWith(".jpeg")
                || lowerUrl.endsWith(".png")
                || lowerUrl.endsWith(".gif")
                || lowerUrl.endsWith(".webp");
    }

    /**
     * Domain rule: Get file extension from image URL
     */
    public String getFileExtension(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(".")) {
            return "";
        }

        return imageUrl.substring(imageUrl.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Domain rule: Check if image is in valid state
     */
    public boolean isValidImageState(ProductImage image) {
        return image != null
                && image.getProductId() != null
                && image.getImageUrl() != null
                && !image.getImageUrl().trim().isEmpty()
                && image.getSortOrder() != null
                && image.getSortOrder() >= 0;
    }

    /**
     * Domain rule: Validate image for creation
     */
    public void validateForCreation(Integer productId, String imageUrl, Integer sortOrder) {
        validateProductId(productId);
        validateImageUrl(imageUrl);
        validateSortOrder(sortOrder);
    }

    /**
     * Domain rule: Validate image for update
     */
    public void validateForUpdate(ProductImage image, Integer newSortOrder) {
        if (image == null) {
            throw new ValidationException("Image cannot be null for update", "IMAGE_NULL");
        }

        if (newSortOrder != null) {
            validateSortOrder(newSortOrder);
        }
    }
}
