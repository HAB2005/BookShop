package com.example.system_backend.product.book.repository;

import com.example.system_backend.product.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

       Optional<Book> findByProductId(Integer productId);

       /**
        * Find books by criteria - pure domain query.
        * Returns Book entities for domain operations.
        */
       @Query("SELECT b FROM Book b WHERE " +
                     "(:keyword IS NULL OR " +
                     "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.language) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                     "(:language IS NULL OR LOWER(b.language) = LOWER(:language)) AND " +
                     "(:publishYear IS NULL OR b.publishYear = :publishYear)")
       Page<Book> findBooksByCriteria(@Param("keyword") String keyword,
                     @Param("language") String language,
                     @Param("publishYear") Integer publishYear,
                     Pageable pageable);

       /**
        * Search books with product information - returns raw data for mapping.
        * Query returns: [productId, name, price, isbn, publishYear, language]
        */
       @Query("SELECT p.productId, p.name, p.price, b.isbn, b.publishYear, b.language " +
                     "FROM Book b JOIN Product p ON b.productId = p.productId WHERE " +
                     "p.status = 'ACTIVE' AND " +
                     "(:keyword IS NULL OR " +
                     "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.language) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                     "(:language IS NULL OR LOWER(b.language) = LOWER(:language)) AND " +
                     "(:publishYear IS NULL OR b.publishYear = :publishYear)")
       Page<Object[]> searchBooksWithProductRaw(@Param("keyword") String keyword,
                     @Param("language") String language,
                     @Param("publishYear") Integer publishYear,
                     Pageable pageable);

       /**
        * Get book suggestions - returns raw data for mapping.
        * Query returns: [productId, name]
        */
       @Query("SELECT p.productId, p.name " +
                     "FROM Book b JOIN Product p ON b.productId = p.productId WHERE " +
                     "p.status = 'ACTIVE' AND " +
                     "(:keyword IS NULL OR " +
                     "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                     "ORDER BY p.createdAt DESC")
       List<Object[]> findBookSuggestionsRaw(@Param("keyword") String keyword,
                     Pageable pageable);
}