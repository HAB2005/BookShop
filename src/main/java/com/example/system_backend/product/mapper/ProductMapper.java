package com.example.system_backend.product.mapper;

import com.example.system_backend.product.application.service.ProductCategoryService;
import com.example.system_backend.product.book.dto.BookInfoResponse;
import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.dto.ProductDetailResponse;
import com.example.system_backend.product.dto.ProductListResponse;
import com.example.system_backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ProductMapper handles mapping between Product entities and response DTOs.
 * Separates mapping logic from business logic in services.
 */
@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductCategoryService productCategoryService;

    /**
     * Map Product entity to ProductDetailResponse DTO
     *
     * @param product the Product entity
     * @param book    the BookInfoResponse (optional)
     * @return ProductDetailResponse DTO
     */
    public ProductDetailResponse mapToDetailResponse(Product product, BookInfoResponse book) {
        ProductDetailResponse.ProductDetailResponseBuilder builder = ProductDetailResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt());

        if (book != null) {
            builder.book(book);
        }

        return builder.build();
    }

    /**
     * Map Product entity to ProductListResponse DTO
     *
     * @param product the Product entity
     * @return ProductListResponse DTO
     */
    public ProductListResponse mapToListResponse(Product product) {
        // Get categories for this product
        List<CategoryResponse> categories = productCategoryMapper.mapToCategoryResponses(
                productCategoryService.getProductCategories(product.getProductId()));

        return ProductListResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .categories(categories)
                .build();
    }
}
