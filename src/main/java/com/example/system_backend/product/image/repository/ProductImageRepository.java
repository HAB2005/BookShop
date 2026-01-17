package com.example.system_backend.product.image.repository;

import com.example.system_backend.product.image.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

        /**
         * Find all images for a product, ordered by sort_order
         */
        List<ProductImage> findByProductIdOrderBySortOrderAsc(Integer productId);

        /**
         * Find primary image for a product
         */
        Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Integer productId);

        /**
         * Count images for a product
         */
        long countByProductId(Integer productId);

        /**
         * Check if product has any images
         */
        boolean existsByProductId(Integer productId);

        /**
         * Delete all images for a product
         */
        void deleteByProductId(Integer productId);

        /**
         * Find images by product ID and sort order range
         */
        List<ProductImage> findByProductIdAndSortOrderBetweenOrderBySortOrderAsc(
                        Integer productId, Integer minOrder, Integer maxOrder);

        /**
         * Get max sort order for a product
         */
        @Query("SELECT COALESCE(MAX(pi.sortOrder), 0) FROM ProductImage pi WHERE pi.productId = :productId")
        Integer getMaxSortOrderByProductId(@Param("productId") Integer productId);

        /**
         * Unset all primary images for a product (used before setting new primary)
         */
        @Modifying
        @Query("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.productId = :productId")
        void unsetAllPrimaryByProductId(@Param("productId") Integer productId);

        /**
         * Update sort orders for images after a specific order
         */
        @Modifying
        @Query("UPDATE ProductImage pi SET pi.sortOrder = pi.sortOrder + 1 "
                        + "WHERE pi.productId = :productId AND pi.sortOrder >= :fromOrder")
        void incrementSortOrdersFrom(@Param("productId") Integer productId, @Param("fromOrder") Integer fromOrder);

        /**
         * Update sort orders for images after a specific order (decrement)
         */
        @Modifying
        @Query("UPDATE ProductImage pi SET pi.sortOrder = pi.sortOrder - 1 "
                        + "WHERE pi.productId = :productId AND pi.sortOrder > :fromOrder")
        void decrementSortOrdersAfter(@Param("productId") Integer productId, @Param("fromOrder") Integer fromOrder);
}
