package com.example.system_backend.stock.adapter;

import com.example.system_backend.common.port.StockQueryPort;
import com.example.system_backend.stock.application.service.StockQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementation of StockQueryPort using actual stock services
 */
@Component
@RequiredArgsConstructor
public class StockQueryAdapter implements StockQueryPort {

    private final StockQueryService stockQueryService;

    @Override
    public boolean hasStock(Integer productId, Integer quantity) {
        return stockQueryService.hasStock(productId, quantity);
    }

    @Override
    public Integer getAvailableQuantity(Integer productId) {
        return stockQueryService.getAvailableQuantity(productId);
    }

    @Override
    public boolean existsByProductId(Integer productId) {
        return stockQueryService.existsByProductId(productId);
    }
}