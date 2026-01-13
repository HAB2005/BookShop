package com.example.system_backend.product.book.mapper;

import com.example.system_backend.product.book.dto.BookInfoResponse;
import com.example.system_backend.product.book.dto.BookSearchResponse;
import com.example.system_backend.product.book.dto.BookSuggestionResponse;
import com.example.system_backend.product.book.entity.Book;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * BookMapper handles mapping from entities and raw query results to DTOs.
 * Pure mapping logic without business concerns.
 */
@Component
public class BookMapper {

    /**
     * Map Book entity to BookInfoResponse.
     */
    public BookInfoResponse mapToBookInfoResponse(Book book) {
        return BookInfoResponse.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .publishYear(book.getPublishYear())
                .pageCount(book.getPageCount())
                .language(book.getLanguage())
                .build();
    }

    /**
     * Map raw query result to BookSearchResponse.
     * Query returns: [productId, name, price, isbn, publishYear, language]
     */
    public BookSearchResponse mapToBookSearchResponse(Object[] row) {
        return BookSearchResponse.builder()
                .productId((Integer) row[0])
                .name((String) row[1])
                .price((BigDecimal) row[2])
                .isbn((String) row[3])
                .publishYear((Integer) row[4])
                .language((String) row[5])
                .build();
    }

    /**
     * Map raw query result to BookSuggestionResponse.
     * Query returns: [productId, name]
     */
    public BookSuggestionResponse mapToBookSuggestionResponse(Object[] row) {
        return BookSuggestionResponse.builder()
                .productId((Integer) row[0])
                .name((String) row[1])
                .build();
    }
}