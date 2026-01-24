package com.example.system_backend.stock.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.stock.entity.Stock;
import com.example.system_backend.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StockQueryService handles read operations for stock data
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockQueryService {

    private final StockRepository stockRepository;

    /**
     * Get stock by product ID
     */
    public Stock getStockByProductId(Integer productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product ID: " + productId));
    }

    /**
     * Get stock by product ID, return null if not found
     */
    public Stock getStockByProductIdOrNull(Integer productId) {
        return stockRepository.findByProductId(productId).orElse(null);
    }

    /**
     * Check if product has stock
     */
    public boolean hasStock(Integer productId, Integer quantity) {
        Stock stock = getStockByProductIdOrNull(productId);
        return stock != null && stock.hasStock(quantity);
    }

    /**
     * Get available quantity for product
     */
    public Integer getAvailableQuantity(Integer productId) {
        Stock stock = getStockByProductIdOrNull(productId);
        return stock != null ? stock.getAvailableQuantity() : 0;
    }

    /**
     * Get all stocks
     */
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    /**
     * Get stocks by product IDs
     */
    public List<Stock> getStocksByProductIds(List<Integer> productIds) {
        return stockRepository.findByProductIds(productIds);
    }

    /**
     * Get stocks by product IDs as map (productId -> stock)
     */
    public Map<Integer, Stock> getStocksMapByProductIds(List<Integer> productIds) {
        return getStocksByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(Stock::getProductId, stock -> stock));
    }

    /**
     * Get low stock items
     */
    public List<Stock> getLowStockItems() {
        return stockRepository.findLowStockItems();
    }

    /**
     * Check if stock exists for product
     */
    public boolean existsByProductId(Integer productId) {
        return stockRepository.existsByProductId(productId);
    }

    /**
     * Get stock statistics
     */
    public StockStatistics getStockStatistics() {
        Long totalQuantity = stockRepository.getTotalStockQuantity();
        Long productsInStock = stockRepository.countProductsInStock();
        Long lowStockProducts = stockRepository.countLowStockProducts();
        
        return StockStatistics.builder()
                .totalStockQuantity(totalQuantity != null ? totalQuantity : 0L)
                .productsInStock(productsInStock != null ? productsInStock : 0L)
                .lowStockProducts(lowStockProducts != null ? lowStockProducts : 0L)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockStatistics {
        private Long totalStockQuantity;
        private Long productsInStock;
        private Long lowStockProducts;
    }
}