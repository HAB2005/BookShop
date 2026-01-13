package com.example.system_backend.product.controller;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.product.application.facade.ProductFacade;
import com.example.system_backend.product.book.dto.BookSearchResponse;
import com.example.system_backend.product.book.dto.BookSuggestionResponse;
import com.example.system_backend.product.dto.AssignCategoriesRequest;
import com.example.system_backend.product.dto.CreateProductRequest;
import com.example.system_backend.product.dto.ProductDetailResponse;
import com.example.system_backend.product.dto.ProductListResponse;
import com.example.system_backend.product.dto.UpdateProductRequest;
import com.example.system_backend.product.dto.UpdateProductStatusRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacade productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductListResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false, defaultValue = "false") boolean includeAllStatuses) {

        PageResponse<ProductListResponse> response = productService.getProducts(
                page, size, sortBy, sortDir, name, minPrice, maxPrice, categoryIds, includeAllStatuses);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Integer productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse response = productService.createProduct(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<Map<String, String>> updateProductStatus(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        productService.updateProductStatus(productId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product status updated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<BookSearchResponse>> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer publishYear) {

        PageResponse<BookSearchResponse> response = productService.searchProducts(
                page, size, sortBy, sortDir, keyword, language, publishYear);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<BookSuggestionResponse>> getProductSuggestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        List<BookSuggestionResponse> response = productService.getProductSuggestions(keyword, limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/categories")
    public ResponseEntity<Map<String, String>> assignCategoriesToProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody AssignCategoriesRequest request) {
        productService.updateProductCategories(productId, request.getCategoryIds());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product categories updated successfully");
        return ResponseEntity.ok(response);
    }
}
