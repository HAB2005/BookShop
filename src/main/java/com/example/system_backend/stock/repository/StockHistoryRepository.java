package com.example.system_backend.stock.repository;

import com.example.system_backend.stock.entity.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer> {

    /**
     * Find history by stock ID with pagination
     */
    Page<StockHistory> findByStockIdOrderByCreatedAtDesc(Integer stockId, Pageable pageable);

    /**
     * Find history by stock ID
     */
    List<StockHistory> findByStockIdOrderByCreatedAtDesc(Integer stockId);

    /**
     * Find history by change type
     */
    List<StockHistory> findByChangeTypeOrderByCreatedAtDesc(StockHistory.ChangeType changeType);

    /**
     * Find history within date range
     */
    @Query("SELECT sh FROM StockHistory sh WHERE sh.createdAt BETWEEN :startDate AND :endDate ORDER BY sh.createdAt DESC")
    List<StockHistory> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent history (last N records)
     */
    @Query("SELECT sh FROM StockHistory sh ORDER BY sh.createdAt DESC")
    Page<StockHistory> findRecentHistory(Pageable pageable);
}