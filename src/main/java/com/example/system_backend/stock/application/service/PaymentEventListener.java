package com.example.system_backend.stock.application.service;

import com.example.system_backend.common.exception.SystemException;
import com.example.system_backend.common.port.OrderQueryPort;
import com.example.system_backend.payment.dto.PaymentSuccessEventData;
import com.example.system_backend.stock.application.facade.StockFacade;
import com.example.system_backend.stock.dto.StockReductionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PaymentEventListener handles payment success events to update stock
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final StockFacade stockFacade;
    private final OrderQueryPort orderQueryPort;

    /**
     * Handle payment success event - reduce stock for order items
     */
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEventData event) {
        log.info("Handling payment success event for order {}", event.getOrderId());

        try {
            // Get order items via port
            List<OrderQueryPort.OrderItemInfoPort> orderItems = orderQueryPort.getOrderItemsForStockReduction(event.getOrderId());

            // Convert to stock reductions
            List<StockReductionData> stockReductions = orderItems.stream()
                    .map(this::toStockReduction)
                    .toList();

            // Process stock reduction
            stockFacade.processOrderStockReduction(event.getOrderId(), stockReductions);

            log.info("Successfully processed stock reduction for order {} with {} items",
                    event.getOrderId(), stockReductions.size());

        } catch (Exception e) {
            log.error("Failed to process stock reduction for order {}: {}",
                    event.getOrderId(), e.getMessage(), e);
            // In production, you might want to implement retry mechanism or dead letter
            // queue
            throw new SystemException("Stock reduction failed for order " + event.getOrderId(), e);
        }
    }

    /**
     * Convert OrderItemInfoPort to StockReductionData
     */
    private StockReductionData toStockReduction(OrderQueryPort.OrderItemInfoPort orderItem) {
        return StockReductionData.builder()
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .build();
    }
}