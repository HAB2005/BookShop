package com.example.system_backend.product.repository;

import com.example.system_backend.product.entity.ProductCategory;
import com.example.system_backend.product.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {

    List<ProductCategory> findByProductId(Integer productId);

    List<ProductCategory> findByCategoryId(Integer categoryId);

    @Query("SELECT pc.productId FROM ProductCategory pc WHERE pc.categoryId = :categoryId")
    List<Integer> findProductIdsByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT pc.productId FROM ProductCategory pc WHERE pc.categoryId IN :categoryIds")
    List<Integer> findProductIdsByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);

    void deleteByProductId(Integer productId);

    void deleteByProductIdAndCategoryId(Integer productId, Integer categoryId);
}
