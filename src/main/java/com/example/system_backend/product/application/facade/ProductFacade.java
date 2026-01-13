package com.example.system_backend.product.application.facade;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.product.application.service.ProductCommandService;
import com.example.system_backend.product.application.service.ProductQueryService;
import com.example.system_backend.product.book.dto.BookInfoResponse;
import com.example.system_backend.product.book.dto.BookSearchResponse;
import com.example.system_backend.product.book.dto.BookSuggestionResponse;
import com.example.system_backend.product.book.dto.CreateBookRequest;
import com.example.system_backend.product.book.mapper.BookMapper;
import com.example.system_backend.product.book.service.BookService;
import com.example.system_backend.product.category.application.facade.CategoryFacade;
import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.dto.CreateProductRequest;
import com.example.system_backend.product.dto.ProductDetailResponse;
import com.example.system_backend.product.dto.ProductListResponse;
import com.example.system_backend.product.dto.UpdateProductRequest;
import com.example.system_backend.product.dto.UpdateProductStatusRequest;
import com.example.system_backend.product.entity.Product;
import com.example.system_backend.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductService acts as a pure orchestrator for cross-domain operations.
 * Coordinates between Product, Book, and Category domains.
 * This is the ONLY service that should do cross-domain orchestration.
 */
@Service
@RequiredArgsConstructor
public class ProductFacade {

    // Domain services
    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;
    private final BookService bookService;
    private final CategoryFacade categoryFacade;

    // Mappers for cross-domain operations
    private final ProductMapper productMapper;
    private final BookMapper bookMapper;

    // Query operations with cross-domain orchestration
    public PageResponse<ProductListResponse> getProducts(int page, int size, String sortBy, String sortDir,
            String name, BigDecimal minPrice, BigDecimal maxPrice, List<Integer> categoryIds,
            boolean includeAllStatuses) {

        // Expand category IDs to include descendants (cross-domain call)
        List<Integer> expandedCategoryIds = null;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            expandedCategoryIds = categoryFacade.getAllDescendantCategoryIds(categoryIds);
        }

        // Get products from ProductQueryService
        PageResponse<Product> productPage = productQueryService.getProductsRaw(
                page, size, sortBy, sortDir, name, minPrice, maxPrice, expandedCategoryIds, includeAllStatuses);

        // Map to response with cross-domain data
        return productPage.map(productMapper::mapToListResponse);
    }

    // Overloaded method for backward compatibility
    public PageResponse<ProductListResponse> getProducts(int page, int size, String sortBy, String sortDir,
            String name, BigDecimal minPrice, BigDecimal maxPrice, List<Integer> categoryIds) {
        return getProducts(page, size, sortBy, sortDir, name, minPrice, maxPrice, categoryIds, false);
    }

    public ProductDetailResponse getProductDetail(Integer productId) {
        // Get product from ProductQueryService
        Product product = productQueryService.getProductById(productId);

        // Get book information (cross-domain call)
        Optional<BookInfoResponse> bookInfo = bookService.getBookByProductId(productId);

        return productMapper.mapToDetailResponse(product, bookInfo.orElse(null));
    }

    public PageResponse<BookSearchResponse> searchProducts(int page, int size, String sortBy, String sortDir,
            String keyword, String language, Integer publishYear) {

        // Application logic: validate and set default values
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 100);
        sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "createdAt";
        sortDir = (sortDir != null && sortDir.equalsIgnoreCase("asc")) ? "asc" : "desc";

        // Create sort object
        Sort sort = sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get raw data from BookService
        Page<Object[]> rawResults = bookService.searchBooksWithProductRaw(keyword, language, publishYear, pageable);

        // Map using BookMapper
        return PageResponse.<BookSearchResponse>builder()
                .content(rawResults.getContent().stream()
                        .map(bookMapper::mapToBookSearchResponse)
                        .toList())
                .page(rawResults.getNumber())
                .size(rawResults.getSize())
                .totalElements(rawResults.getTotalElements())
                .totalPages(rawResults.getTotalPages())
                .first(rawResults.isFirst())
                .last(rawResults.isLast())
                .empty(rawResults.isEmpty())
                .build();
    }

    public List<BookSuggestionResponse> getProductSuggestions(String keyword, int limit) {
        // Application logic: validate limit
        limit = Math.min(Math.max(1, limit), 20);

        Pageable pageable = PageRequest.of(0, limit);

        // Get raw data from BookService
        List<Object[]> rawResults = bookService.getBookSuggestionsRaw(keyword, pageable);

        // Map using BookMapper
        return rawResults.stream()
                .map(bookMapper::mapToBookSuggestionResponse)
                .toList();
    }

    // Command operations with cross-domain orchestration
    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        // Validate categories first (cross-domain validation)
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            validateCategories(request.getCategoryIds());
        }

        // Create product
        Product savedProduct = productCommandService.createProduct(request);

        // Assign categories (cross-domain operation)
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            productCommandService.assignCategoriesToProduct(savedProduct.getProductId(), request.getCategoryIds());
        }

        // Create book if provided (cross-domain operation)
        BookInfoResponse bookInfo = null;
        if (request.getBook() != null) {
            bookInfo = bookService.createBook(savedProduct.getProductId(), request.getBook());
        }

        return productMapper.mapToDetailResponse(savedProduct, bookInfo);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Integer productId, UpdateProductRequest request) {
        // Validate categories first (cross-domain validation)
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            validateCategories(request.getCategoryIds());
        }

        // Update product
        Product updatedProduct = productCommandService.updateProduct(productId, request);

        // Update categories (cross-domain operation)
        if (request.getCategoryIds() != null) {
            productCommandService.assignCategoriesToProduct(productId, request.getCategoryIds());
        }

        // Update book if provided (cross-domain operation with application logic)
        BookInfoResponse bookInfo = null;
        if (request.getBook() != null) {
            // Application logic: create or update based on existence
            Optional<BookInfoResponse> existingBook = bookService.getBookByProductId(productId);
            if (existingBook.isPresent()) {
                bookInfo = bookService.updateBook(productId, request.getBook());
            } else {
                // Convert UpdateBookRequest to CreateBookRequest
                CreateBookRequest createRequest = CreateBookRequest.builder()
                        .isbn(request.getBook().getIsbn())
                        .description(request.getBook().getDescription())
                        .publishYear(request.getBook().getPublishYear())
                        .pageCount(request.getBook().getPageCount())
                        .language(request.getBook().getLanguage())
                        .build();
                bookInfo = bookService.createBook(productId, createRequest);
            }
        } else {
            bookInfo = bookService.getBookByProductId(productId).orElse(null);
        }

        return productMapper.mapToDetailResponse(updatedProduct, bookInfo);
    }

    public void updateProductStatus(Integer productId, UpdateProductStatusRequest request) {
        // Pure delegation to ProductCommandService
        productCommandService.updateProductStatus(productId, request);
    }

    @Transactional
    public void updateProductCategories(Integer productId, List<Integer> categoryIds) {
        // Validate categories (cross-domain validation)
        if (categoryIds != null && !categoryIds.isEmpty()) {
            validateCategories(categoryIds);
        }

        // Update categories
        productCommandService.assignCategoriesToProduct(productId, categoryIds);
    }

    // Helper methods for cross-domain operations
    private void validateCategories(List<Integer> categoryIds) {
        for (Integer categoryId : categoryIds) {
            CategoryResponse category = categoryFacade.getCategoryDetail(categoryId);
            if (!"ACTIVE".equals(category.getStatus())) {
                throw new IllegalArgumentException("Category " + categoryId + " is not active");
            }
        }
    }
}