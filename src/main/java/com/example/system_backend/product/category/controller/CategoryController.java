package com.example.system_backend.product.category.controller;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.product.category.application.facade.CategoryFacade;
import com.example.system_backend.product.category.dto.CategoryResponse;
import com.example.system_backend.product.category.dto.CreateCategoryRequest;
import com.example.system_backend.product.category.dto.UpdateCategoryRequest;
import com.example.system_backend.product.category.dto.UpdateCategoryStatusRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryFacade categoryFacade;

    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer parentId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        PageResponse<CategoryResponse> response = categoryFacade.getCategories(
                page, size, sortBy, sortDir, name, parentId, includeInactive);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryDetail(@PathVariable Integer categoryId) {
        CategoryResponse response = categoryFacade.getCategoryDetail(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryFacade.getCategoryBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryFacade.createCategory(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryFacade.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}/status")
    public ResponseEntity<Map<String, String>> updateCategoryStatus(
            @PathVariable Integer categoryId,
            @Valid @RequestBody UpdateCategoryStatusRequest request) {
        categoryFacade.updateCategoryStatus(categoryId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Category status updated successfully");
        return ResponseEntity.ok(response);
    }
}