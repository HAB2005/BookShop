package com.example.system_backend.stock.controller;

import com.example.system_backend.common.response.SuccessResponse;
import com.example.system_backend.stock.application.facade.StockFacade;
import com.example.system_backend.stock.application.service.StockQueryService;
import com.example.system_backend.stock.dto.CreateStockRequest;
import com.example.system_backend.stock.dto.StockResponse;
import com.example.system_backend.stock.dto.UpdateStockRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * StockController handles stock management operations
 * Admin only endpoints for stock management
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockFacade stockFacade;

    /**
     * Get stock information for a product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<SuccessResponse<StockResponse>> getStockByProductId(@PathVariable Integer productId) {
        log.info("Getting stock for product: {}", productId);
        StockResponse stock = stockFacade.getStockByProductId(productId);
        return ResponseEntity.ok(SuccessResponse.success(stock));
    }

    /**
     * Get stock information for multiple products
     */
    @GetMapping("/products")
    public ResponseEntity<SuccessResponse<Map<Integer, StockResponse>>> getStocksByProductIds(
            @RequestParam List<Integer> productIds) {
        log.info("Getting stocks for products: {}", productIds);
        Map<Integer, StockResponse> stocks = stockFacade.getStocksByProductIds(productIds);
        return ResponseEntity.ok(SuccessResponse.success(stocks));
    }

    /**
     * Create stock for a product
     */
    @PostMapping("/product/{productId}")
    public ResponseEntity<SuccessResponse<StockResponse>> createStock(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") Integer initialQuantity,
            @RequestParam(defaultValue = "5") Integer lowStockThreshold) {

        log.info("Creating stock for product {}: {} units, threshold: {}",
                productId, initialQuantity, lowStockThreshold);

        StockResponse stock = stockFacade.createStock(productId, initialQuantity, lowStockThreshold);
        return ResponseEntity.ok(SuccessResponse.success(stock));
    }

    /**
     * Create stock using request body
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<StockResponse>> createStockFromRequest(
            @Valid @RequestBody CreateStockRequest request) {
        
        log.info("Creating stock from request: {}", request);
        
        StockResponse stock = stockFacade.createStock(
                request.getProductId(), 
                request.getInitialQuantity(), 
                request.getLowStockThreshold());
        return ResponseEntity.ok(SuccessResponse.success(stock));
    }

    /**
     * Add stock (restock)
     */
    @PostMapping("/product/{productId}/add")
    public ResponseEntity<SuccessResponse<StockResponse>> addStock(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("Adding stock for product {}: {} units", productId, request.getQuantity());

        StockResponse stock = stockFacade.addStock(productId, request.getQuantity(), request.getReason());
        return ResponseEntity.ok(SuccessResponse.success(stock));
    }

    /**
     * Set stock quantity (adjust)
     */
    @PutMapping("/product/{productId}")
    public ResponseEntity<SuccessResponse<StockResponse>> setStock(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("Setting stock for product {}: {} units", productId, request.getQuantity());

        StockResponse stock = stockFacade.setStock(productId, request.getQuantity(), request.getReason());
        return ResponseEntity.ok(SuccessResponse.success(stock));
    }

    /**
     * Get low stock items
     */
    @GetMapping("/low-stock")
    public ResponseEntity<SuccessResponse<List<StockResponse>>> getLowStockItems() {
        log.info("Getting low stock items");
        List<StockResponse> lowStockItems = stockFacade.getLowStockItems();
        return ResponseEntity.ok(SuccessResponse.success(lowStockItems));
    }

    /**
     * Get stock statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SuccessResponse<StockQueryService.StockStatistics>> getStockStatistics() {
        log.info("Getting stock statistics");
        StockQueryService.StockStatistics statistics = stockFacade.getStockStatistics();
        return ResponseEntity.ok(SuccessResponse.success(statistics));
    }
}