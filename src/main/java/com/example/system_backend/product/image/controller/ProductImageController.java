package com.example.system_backend.product.image.controller;

import com.example.system_backend.product.image.application.facade.ProductImageFacade;
import com.example.system_backend.product.image.dto.ProductImageResponse;
import com.example.system_backend.product.image.dto.ReorderImagesRequest;
import com.example.system_backend.product.image.dto.UpdateImageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageFacade productImageFacade;

    /**
     * Upload image for a product
     */
    @PostMapping
    public ResponseEntity<ProductImageResponse> uploadImage(
            @RequestParam("productId") Integer productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") Boolean isPrimary,
            @RequestParam(required = false) Integer sortOrder) {

        ProductImageResponse response = productImageFacade.uploadImage(file, productId, isPrimary, sortOrder);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all images for a product
     */
    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@RequestParam Integer productId) {
        List<ProductImageResponse> response = productImageFacade.getProductImages(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get primary image for a product
     */
    @GetMapping("/primary")
    public ResponseEntity<ProductImageResponse> getPrimaryImage(@RequestParam Integer productId) {
        return productImageFacade.getPrimaryImage(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get image by ID
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<ProductImageResponse> getImageById(@PathVariable Integer imageId) {
        ProductImageResponse response = productImageFacade.getImageById(imageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update image properties
     */
    @PutMapping("/{imageId}")
    public ResponseEntity<ProductImageResponse> updateImage(
            @PathVariable Integer imageId,
            @Valid @RequestBody UpdateImageRequest request) {

        ProductImageResponse response = productImageFacade.updateImage(imageId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Set image as primary
     */
    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<ProductImageResponse> setPrimaryImage(@PathVariable Integer imageId) {
        ProductImageResponse response = productImageFacade.setPrimaryImage(imageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete image
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Integer imageId) {
        productImageFacade.deleteImage(imageId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Image deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Reorder images for a product
     */
    @PutMapping("/reorder")
    public ResponseEntity<Map<String, String>> reorderImages(
            @Valid @RequestBody ReorderImagesRequest request) {

        productImageFacade.reorderImages(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Images reordered successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete all images for a product
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAllProductImages(@RequestParam Integer productId) {
        productImageFacade.deleteAllProductImages(productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All images deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get image statistics for a product
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getImageStats(@RequestParam Integer productId) {
        long imageCount = productImageFacade.getImageCount(productId);
        boolean hasImages = productImageFacade.hasImages(productId);
        boolean hasPrimary = productImageFacade.getPrimaryImage(productId).isPresent();

        Map<String, Object> stats = new HashMap<>();
        stats.put("imageCount", imageCount);
        stats.put("hasImages", hasImages);
        stats.put("hasPrimary", hasPrimary);

        return ResponseEntity.ok(stats);
    }
}
