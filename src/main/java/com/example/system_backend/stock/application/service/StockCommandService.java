package com.example.system_backend.stock.application.service;

import com.example.system_backend.common.exception.BusinessException;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.stock.entity.Stock;
import com.example.system_backend.stock.entity.StockHistory;
import com.example.system_backend.stock.repository.StockHistoryRepository;
import com.example.system_backend.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * StockCommandService handles write operations for stock data
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockCommandService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;

    /**
     * Create stock for product
     */
    public Stock createStock(Integer productId, Integer initialQuantity, Integer lowStockThreshold) {
        if (stockRepository.existsByProductId(productId)) {
            throw new BusinessException("Stock already exists for product ID: " + productId);
        }

        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setAvailableQuantity(initialQuantity);
        stock.setLowStockThreshold(lowStockThreshold != null ? lowStockThreshold : 5);

        Stock savedStock = stockRepository.save(stock);

        // Record history
        if (initialQuantity > 0) {
            recordStockHistory(savedStock.getStockId(), StockHistory.ChangeType.IN,
                    initialQuantity, "Initial stock");
        }

        log.info("Created stock for product {}: {} units", productId, initialQuantity);
        return savedStock;
    }

    /**
     * Add stock (restock)
     */
    public Stock addStock(Integer productId, Integer quantity, String reason) {
        Stock stock = getStockByProductId(productId);

        Integer oldQuantity = stock.getAvailableQuantity();
        stock.addStock(quantity);

        Stock savedStock = stockRepository.save(stock);

        // Record history
        recordStockHistory(stock.getStockId(), StockHistory.ChangeType.IN, quantity,
                reason != null ? reason : "Stock added");

        log.info("Added stock for product {}: {} units (from {} to {})",
                productId, quantity, oldQuantity, savedStock.getAvailableQuantity());

        return savedStock;
    }

    /**
     * Reduce stock (when payment successful)
     */
    public Stock reduceStock(Integer productId, Integer quantity, String reason) {
        Stock stock = getStockByProductId(productId);

        if (!stock.hasStock(quantity)) {
            throw new BusinessException("Insufficient stock for product ID: " + productId +
                    ". Available: " + stock.getAvailableQuantity() +
                    ", Required: " + quantity);
        }

        Integer oldQuantity = stock.getAvailableQuantity();
        stock.reduceStock(quantity);

        Stock savedStock = stockRepository.save(stock);

        // Record history
        recordStockHistory(stock.getStockId(), StockHistory.ChangeType.OUT, quantity,
                reason != null ? reason : "Stock sold");

        log.info("Reduced stock for product {}: {} units (from {} to {})",
                productId, quantity, oldQuantity, savedStock.getAvailableQuantity());

        return savedStock;
    }

    /**
     * Set stock quantity (adjust)
     */
    public Stock setStock(Integer productId, Integer newQuantity, String reason) {
        Stock stock = getStockByProductId(productId);

        Integer oldQuantity = stock.getAvailableQuantity();
        Integer difference = newQuantity - oldQuantity;

        stock.setStock(newQuantity);
        Stock savedStock = stockRepository.save(stock);

        // Record history
        if (difference != 0) {
            StockHistory.ChangeType changeType = difference > 0 ? StockHistory.ChangeType.IN
                    : StockHistory.ChangeType.OUT;
            recordStockHistory(stock.getStockId(), changeType, Math.abs(difference),
                    reason != null ? reason : "Stock adjusted");
        }

        log.info("Set stock for product {}: {} units (from {} to {})",
                productId, newQuantity, oldQuantity, savedStock.getAvailableQuantity());

        return savedStock;
    }

    /**
     * Update low stock threshold
     */
    public Stock updateLowStockThreshold(Integer productId, Integer threshold) {
        Stock stock = getStockByProductId(productId);
        stock.setLowStockThreshold(threshold);

        Stock savedStock = stockRepository.save(stock);
        log.info("Updated low stock threshold for product {}: {}", productId, threshold);

        return savedStock;
    }

    /**
     * Reduce stock for multiple products (for order processing)
     */
    public void reduceStockForOrder(List<StockReduction> reductions, String reason) {
        for (StockReduction reduction : reductions) {
            reduceStock(reduction.getProductId(), reduction.getQuantity(), reason);
        }
    }

    /**
     * Delete stock (when product is deleted)
     */
    public void deleteStock(Integer productId) {
        Stock stock = getStockByProductId(productId);
        stockRepository.delete(stock);
        log.info("Deleted stock for product {}", productId);
    }

    /**
     * Get stock by product ID with exception if not found
     */
    private Stock getStockByProductId(Integer productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product ID: " + productId));
    }

    /**
     * Record stock history
     */
    private void recordStockHistory(Integer stockId, StockHistory.ChangeType changeType,
            Integer quantity, String reason) {
        StockHistory history = new StockHistory(stockId, changeType, quantity, reason);
        stockHistoryRepository.save(history);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockReduction {
        private Integer productId;
        private Integer quantity;
    }
}