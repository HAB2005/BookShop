package com.example.system_backend.product.repository;

import com.example.system_backend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

        Page<Product> findByStatus(Product.Status status, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE "
                        + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                        + "(:status IS NULL OR p.status = :status) AND "
                        + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
                        + "(:maxPrice IS NULL OR p.price <= :maxPrice)")
        Page<Product> findProductsWithFilters(@Param("name") String name,
                        @Param("status") Product.Status status,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        Pageable pageable);

        @Query("SELECT p FROM Product p WHERE "
                        + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                        + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
                        + "(:maxPrice IS NULL OR p.price <= :maxPrice)")
        Page<Product> findProductsWithFiltersAllStatuses(@Param("name") String name,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        Pageable pageable);

        @Query("SELECT DISTINCT p FROM Product p "
                        + "LEFT JOIN ProductCategory pc ON p.productId = pc.productId "
                        + "WHERE "
                        + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                        + "(:status IS NULL OR p.status = :status) AND "
                        + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
                        + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
                        + "(:categoryIds IS NULL OR pc.categoryId IN :categoryIds)")
        Page<Product> findProductsWithCategoryFilters(@Param("name") String name,
                        @Param("status") Product.Status status,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        @Param("categoryIds") List<Integer> categoryIds,
                        Pageable pageable);

        @Query("SELECT DISTINCT p FROM Product p "
                        + "LEFT JOIN ProductCategory pc ON p.productId = pc.productId "
                        + "WHERE "
                        + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                        + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
                        + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
                        + "(:categoryIds IS NULL OR pc.categoryId IN :categoryIds)")
        Page<Product> findProductsWithCategoryFiltersAllStatuses(@Param("name") String name,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        @Param("categoryIds") List<Integer> categoryIds,
                        Pageable pageable);

        Page<Product> findByProductIdIn(List<Integer> productIds, Pageable pageable);
}
