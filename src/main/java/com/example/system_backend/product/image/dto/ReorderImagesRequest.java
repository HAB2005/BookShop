package com.example.system_backend.product.image.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderImagesRequest {
    
    @NotNull(message = "Product ID is required")
    private Integer productId;
    
    @NotEmpty(message = "Image order list cannot be empty")
    private List<ImageOrderItem> imageOrders;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageOrderItem {
        @NotNull(message = "Image ID is required")
        private Integer imageId;
        
        @NotNull(message = "Sort order is required")
        private Integer sortOrder;
    }
}