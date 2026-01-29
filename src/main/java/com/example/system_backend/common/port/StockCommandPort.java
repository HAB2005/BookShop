package com.example.system_backend.common.port;

/**
 * StockCommandPort allows other modules to modify stock
 * without direct dependency on Stock module
 */
public interface StockCommandPort {
    
    /**
     * Reduce stock quantity for a product
     * 
     * @param productId Product ID
     * @param quantity Quantity to reduce
     * @return true if successful, false if insufficient stock
     */
    boolean reduceStock(Integer productId, Integer quantity);
    
    /**
     * Increase stock quantity for a product (for order cancellation)
     * 
     * @param productId Product ID
     * @param quantity Quantity to increase
     */
    void increaseStock(Integer productId, Integer quantity);
    
    /**
     * Reserve stock for order (optional - for more complex stock management)
     * 
     * @param productId Product ID
     * @param quantity Quantity to reserve
     * @return reservation ID if successful, null if insufficient stock
     */
    String reserveStock(Integer productId, Integer quantity);
    
    /**
     * Release reserved stock (optional)
     * 
     * @param reservationId Reservation ID
     */
    void releaseReservedStock(String reservationId);
}