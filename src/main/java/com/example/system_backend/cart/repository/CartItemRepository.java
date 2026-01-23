package com.example.system_backend.cart.repository;

import com.example.system_backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * Find cart item by cart ID and product ID
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") Integer productId);

    /**
     * Find all cart items by cart ID
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId ORDER BY ci.addedAt DESC")
    List<CartItem> findByCartId(@Param("cartId") Integer cartId);

    /**
     * Delete all cart items by cart ID
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Integer cartId);

    /**
     * Count items in cart
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Long countByCartId(@Param("cartId") Integer cartId);
}