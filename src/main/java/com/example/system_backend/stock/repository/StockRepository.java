package com.example.system_backend.stock.repository;

import com.example.system_backend.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {

    /**
     * Find stock by product ID
     */
    Optional<Stock> findByProductId(Integer productId);

    /**
     * Check if stock exists for product
     */
    boolean existsByProductId(Integer productId);

    /**
     * Find stocks with low stock (below threshold)
     */
    @Query("SELECT s FROM Stock s WHERE s.availableQuantity <= s.lowStockThreshold")
    List<Stock> findLowStockItems();

    /**
     * Find stocks by product IDs
     */
    @Query("SELECT s FROM Stock s WHERE s.productId IN :productIds")
    List<Stock> findByProductIds(@Param("productIds") List<Integer> productIds);

    /**
     * Get total stock value (for reporting)
     */
    @Query("SELECT SUM(s.availableQuantity) FROM Stock s")
    Long getTotalStockQuantity();

    /**
     * Count products with stock
     */
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.availableQuantity > 0")
    Long countProductsInStock();

    /**
     * Count products with low stock
     */
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.availableQuantity <= s.lowStockThreshold")
    Long countLowStockProducts();
}