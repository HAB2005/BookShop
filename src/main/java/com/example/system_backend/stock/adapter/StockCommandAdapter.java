package com.example.system_backend.stock.adapter;

import com.example.system_backend.common.port.StockCommandPort;
import com.example.system_backend.stock.application.service.StockCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementation of StockCommandPort using actual stock services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockCommandAdapter implements StockCommandPort {

    private final StockCommandService stockCommandService;

    @Override
    public boolean reduceStock(Integer productId, Integer quantity) {
        try {
            stockCommandService.reduceStock(productId, quantity, "Order checkout");
            return true;
        } catch (Exception e) {
            log.error("Failed to reduce stock for product {} by {} units: {}", 
                     productId, quantity, e.getMessage());
            return false;
        }
    }

    @Override
    public void increaseStock(Integer productId, Integer quantity) {
        try {
            stockCommandService.addStock(productId, quantity, "Order cancellation");
        } catch (Exception e) {
            log.error("Failed to increase stock for product {} by {} units: {}", 
                     productId, quantity, e.getMessage());
        }
    }

    @Override
    public String reserveStock(Integer productId, Integer quantity) {
        // TODO: Implement stock reservation if needed
        // For now, just return a dummy reservation ID
        log.info("Stock reservation not implemented - product {} quantity {}", productId, quantity);
        return "RESERVATION_" + productId + "_" + System.currentTimeMillis();
    }

    @Override
    public void releaseReservedStock(String reservationId) {
        // TODO: Implement reservation release if needed
        log.info("Stock reservation release not implemented - reservation {}", reservationId);
    }
}