package com.example.system_backend.cart.repository;

import com.example.system_backend.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Integer userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUserId(Integer userId);

    /**
     * Find cart with items by user ID
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Integer userId);
}