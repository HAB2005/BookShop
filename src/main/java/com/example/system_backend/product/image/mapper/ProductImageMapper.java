package com.example.system_backend.product.image.mapper;

import com.example.system_backend.product.image.dto.ProductImageResponse;
import com.example.system_backend.product.image.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductImageMapper {

    /**
     * Map ProductImage entity to ProductImageResponse DTO
     */
    public ProductImageResponse mapToResponse(ProductImage productImage) {
        if (productImage == null) {
            return null;
        }

        return ProductImageResponse.builder()
                .imageId(productImage.getImageId())
                .productId(productImage.getProductId())
                .imageUrl(productImage.getImageUrl())
                .isPrimary(productImage.getIsPrimary())
                .sortOrder(productImage.getSortOrder())
                .createdAt(productImage.getCreatedAt())
                .build();
    }

    /**
     * Map list of ProductImage entities to list of ProductImageResponse DTOs
     */
    public List<ProductImageResponse> mapToResponseList(List<ProductImage> productImages) {
        if (productImages == null) {
            return null;
        }

        return productImages.stream()
                .map(this::mapToResponse)
                .toList();
    }
}
