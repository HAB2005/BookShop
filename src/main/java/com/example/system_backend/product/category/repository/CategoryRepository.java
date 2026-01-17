package com.example.system_backend.product.category.repository;

import com.example.system_backend.common.enums.CategoryStatus;
import com.example.system_backend.product.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

        Optional<Category> findBySlug(String slug);

        List<Category> findByParentIdIsNullAndStatus(CategoryStatus status);

        List<Category> findByParentIdAndStatus(Integer parentId, CategoryStatus status);

        Page<Category> findByStatus(CategoryStatus status, Pageable pageable);

        @Query("SELECT c FROM Category c WHERE "
                        + "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                        + "(:status IS NULL OR c.status = :status) AND "
                        + "(:parentId IS NULL OR c.parentId = :parentId)")
        Page<Category> findCategoriesWithFilters(@Param("name") String name,
                        @Param("status") CategoryStatus status,
                        @Param("parentId") Integer parentId,
                        Pageable pageable);

        // Admin method to get all categories including inactive ones
        @Query("SELECT c FROM Category c WHERE "
                        + "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                        + "(:parentId IS NULL OR c.parentId = :parentId)")
        Page<Category> findAllCategoriesWithFilters(@Param("name") String name,
                        @Param("parentId") Integer parentId,
                        Pageable pageable);

        boolean existsBySlug(String slug);

        boolean existsBySlugAndCategoryIdNot(String slug, Integer categoryId);

        // Recursive query to get all descendant categories
        @Query(value = "WITH RECURSIVE category_tree AS ("
                        + "  SELECT category_id, name, slug, parent_id, status, created_at "
                        + "  FROM category "
                        + "  WHERE category_id IN :rootCategoryIds AND status = 'ACTIVE' "
                        + "  UNION ALL "
                        + "  SELECT c.category_id, c.name, c.slug, c.parent_id, c.status, c.created_at "
                        + "  FROM category c "
                        + "  INNER JOIN category_tree ct ON c.parent_id = ct.category_id "
                        + "  WHERE c.status = 'ACTIVE' "
                        + ") "
                        + "SELECT category_id FROM category_tree", nativeQuery = true)
        List<Integer> findAllDescendantCategoryIds(@Param("rootCategoryIds") List<Integer> rootCategoryIds);

        // Alternative method using JPQL for databases that don't support CTE
        @Query("SELECT c FROM Category c WHERE c.parentId IN :parentIds AND c.status = 'ACTIVE'")
        List<Category> findByParentIdInAndStatus(@Param("parentIds") List<Integer> parentIds, CategoryStatus status);
}
