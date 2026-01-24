package com.example.system_backend.common.port;

import java.util.List;

/**
 * OrderQueryPort allows other modules to query order information
 * without direct dependency on Order module
 */
public interface OrderQueryPort {
    
    /**
     * Get order details for stock reduction
     */
    List<OrderItemInfoPort> getOrderItemsForStockReduction(Integer orderId);
    
    /**
     * Order item information for stock operations
     */
    interface OrderItemInfoPort {
        Integer getProductId();
        Integer getQuantity();
    }
}