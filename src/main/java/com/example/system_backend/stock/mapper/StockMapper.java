package com.example.system_backend.stock.mapper;

import com.example.system_backend.stock.dto.StockResponse;
import com.example.system_backend.stock.entity.Stock;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StockMapper handles mapping between Stock entities and DTOs
 */
@Component
public class StockMapper {

    /**
     * Map Stock entity to StockResponse DTO
     */
    public StockResponse toResponse(Stock stock) {
        if (stock == null) {
            return null;
        }

        return StockResponse.builder()
                .stockId(stock.getStockId())
                .productId(stock.getProductId())
                .availableQuantity(stock.getAvailableQuantity())
                .lowStockThreshold(stock.getLowStockThreshold())
                .isLowStock(stock.isLowStock())
                .createdAt(stock.getCreatedAt())
                .build();
    }

    /**
     * Map Stock entity to StockResponse DTO with product name
     */
    public StockResponse toResponse(Stock stock, String productName) {
        StockResponse response = toResponse(stock);
        if (response != null) {
            response.setProductName(productName);
        }
        return response;
    }

    /**
     * Map list of Stock entities to list of StockResponse DTOs
     */
    public List<StockResponse> toResponseList(List<Stock> stocks) {
        if (stocks == null) {
            return null;
        }

        return stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Stock entities map to StockResponse DTOs map
     */
    public Map<Integer, StockResponse> toResponseMap(Map<Integer, Stock> stocksMap) {
        if (stocksMap == null) {
            return null;
        }

        return stocksMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toResponse(entry.getValue())
                ));
    }
}