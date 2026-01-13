package com.example.system_backend.product.book.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.product.book.domain.BookValidationService;
import com.example.system_backend.product.book.dto.BookInfoResponse;
import com.example.system_backend.product.book.dto.CreateBookRequest;
import com.example.system_backend.product.book.dto.UpdateBookRequest;
import com.example.system_backend.product.book.entity.Book;
import com.example.system_backend.product.book.mapper.BookMapper;
import com.example.system_backend.product.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * BookService handles PURE Book domain logic only.
 * No application concerns (pagination, response building, cross-domain
 * orchestration).
 * All public access should go through ProductFacade.
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookValidationService bookValidationService;

    /**
     * Get book information by product ID.
     * Pure domain query - used internally by ProductFacade.
     */
    public Optional<BookInfoResponse> getBookByProductId(Integer productId) {
        return bookRepository.findByProductId(productId)
                .map(bookMapper::mapToBookInfoResponse);
    }

    /**
     * Create book for a product.
     * Pure domain operation - used internally by ProductFacade.
     */
    @Transactional
    public BookInfoResponse createBook(Integer productId, CreateBookRequest request) {
        // Validate using domain service
        bookValidationService.validateBookFields(
                request.getIsbn(),
                request.getDescription(),
                request.getPublishYear(),
                request.getPageCount(),
                request.getLanguage());

        Book book = new Book();
        book.setProductId(productId);
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublishYear(request.getPublishYear());
        book.setPageCount(request.getPageCount());
        book.setLanguage(request.getLanguage());

        Book savedBook = bookRepository.save(book);
        return bookMapper.mapToBookInfoResponse(savedBook);
    }

    /**
     * Update book information using domain validation service.
     * Clean domain operation - validation handled by domain service.
     */
    @Transactional
    public BookInfoResponse updateBook(Integer productId, UpdateBookRequest request) {
        Book book = bookRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "productId", productId));

        // Validate and update using domain service
        if (request.getIsbn() != null) {
            bookValidationService.validateIsbn(request.getIsbn());
            book.setIsbn(request.getIsbn());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getPublishYear() != null) {
            bookValidationService.validatePublishYear(request.getPublishYear());
            book.setPublishYear(request.getPublishYear());
        }
        if (request.getPageCount() != null) {
            bookValidationService.validatePageCount(request.getPageCount());
            book.setPageCount(request.getPageCount());
        }
        if (request.getLanguage() != null) {
            bookValidationService.validateLanguage(request.getLanguage());
            book.setLanguage(request.getLanguage());
        }

        Book updatedBook = bookRepository.save(book);
        return bookMapper.mapToBookInfoResponse(updatedBook);
    }

    /**
     * Find books by criteria - pure domain query.
     * Returns Book entities for further domain operations.
     */
    public Page<Book> findBooksByCriteria(String keyword, String language, Integer publishYear, Pageable pageable) {
        return bookRepository.findBooksByCriteria(keyword, language, publishYear, pageable);
    }

    /**
     * Search books with product information - returns raw data for mapping.
     * Used by ProductFacade for application-level operations.
     */
    public Page<Object[]> searchBooksWithProductRaw(String keyword, String language, Integer publishYear,
            Pageable pageable) {
        return bookRepository.searchBooksWithProductRaw(keyword, language, publishYear, pageable);
    }

    /**
     * Get book suggestions - returns raw data for mapping.
     * Used by ProductFacade for application-level operations.
     */
    public List<Object[]> getBookSuggestionsRaw(String keyword, Pageable pageable) {
        return bookRepository.findBookSuggestionsRaw(keyword, pageable);
    }
}