package com.example.system_backend.stock.application.facade;

import com.example.system_backend.stock.application.service.StockCommandService;
import com.example.system_backend.stock.application.service.StockQueryService;
import com.example.system_backend.stock.dto.StockCheckItemData;
import com.example.system_backend.stock.dto.StockReductionData;
import com.example.system_backend.stock.dto.StockResponse;
import com.example.system_backend.stock.entity.Stock;
import com.example.system_backend.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * StockFacade orchestrates stock operations and cross-domain interactions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockFacade {

    private final StockQueryService stockQueryService;
    private final StockCommandService stockCommandService;
    private final StockMapper stockMapper;

    /**
     * Get stock information for product
     */
    @Transactional(readOnly = true)
    public StockResponse getStockByProductId(Integer productId) {
        Stock stock = stockQueryService.getStockByProductId(productId);
        return stockMapper.toResponse(stock);
    }

    /**
     * Check if products have sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(List<StockCheckItemData> items) {
        for (StockCheckItemData item : items) {
            if (!stockQueryService.hasStock(item.getProductId(), item.getQuantity())) {
                log.warn("Insufficient stock for product {}: required {}, available {}", 
                        item.getProductId(), item.getQuantity(), 
                        stockQueryService.getAvailableQuantity(item.getProductId()));
                return false;
            }
        }
        return true;
    }

    /**
     * Get stock information for multiple products
     */
    @Transactional(readOnly = true)
    public Map<Integer, StockResponse> getStocksByProductIds(List<Integer> productIds) {
        Map<Integer, Stock> stocksMap = stockQueryService.getStocksMapByProductIds(productIds);
        return stockMapper.toResponseMap(stocksMap);
    }

    /**
     * Create stock for new product
     */
    @Transactional
    public StockResponse createStock(Integer productId, Integer initialQuantity, Integer lowStockThreshold) {
        Stock stock = stockCommandService.createStock(productId, initialQuantity, lowStockThreshold);
        return stockMapper.toResponse(stock);
    }

    /**
     * Add stock (restock)
     */
    @Transactional
    public StockResponse addStock(Integer productId, Integer quantity, String reason) {
        Stock stock = stockCommandService.addStock(productId, quantity, reason);
        return stockMapper.toResponse(stock);
    }

    /**
     * Set stock quantity
     */
    @Transactional
    public StockResponse setStock(Integer productId, Integer newQuantity, String reason) {
        Stock stock = stockCommandService.setStock(productId, newQuantity, reason);
        return stockMapper.toResponse(stock);
    }

    /**
     * Process stock reduction for order (called after successful payment)
     */
    @Transactional
    public void processOrderStockReduction(Integer orderId, List<StockReductionData> reductions) {
        String reason = "Order #" + orderId + " payment confirmed";
        
        List<StockCommandService.StockReduction> serviceReductions = reductions.stream()
                .map(r -> StockCommandService.StockReduction.builder()
                        .productId(r.getProductId())
                        .quantity(r.getQuantity())
                        .build())
                .toList();
        
        stockCommandService.reduceStockForOrder(serviceReductions, reason);
        log.info("Processed stock reduction for order {}: {} items", orderId, reductions.size());
    }

    /**
     * Get low stock items
     */
    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockItems() {
        List<Stock> lowStockItems = stockQueryService.getLowStockItems();
        return stockMapper.toResponseList(lowStockItems);
    }

    /**
     * Get stock statistics
     */
    @Transactional(readOnly = true)
    public StockQueryService.StockStatistics getStockStatistics() {
        return stockQueryService.getStockStatistics();
    }
}