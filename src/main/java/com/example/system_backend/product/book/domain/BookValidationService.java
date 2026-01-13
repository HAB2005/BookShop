package com.example.system_backend.product.book.domain;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.product.book.entity.Book;
import com.example.system_backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * BookValidationService encapsulates all book domain validation rules.
 * Pure domain logic - no application concerns.
 */
@Service
@RequiredArgsConstructor
public class BookValidationService {

    /**
     * Validate ISBN format
     */
    public void validateIsbn(String isbn) {
        if (isbn != null && isbn.length() > 20) {
            throw new ValidationException("ISBN cannot exceed 20 characters", "ISBN_TOO_LONG");
        }
        // Additional ISBN format validation can be added here
    }

    /**
     * Validate page count
     */
    public void validatePageCount(Integer pageCount) {
        if (pageCount != null && pageCount <= 0) {
            throw new ValidationException("Page count must be positive", "PAGE_COUNT_INVALID");
        }
    }

    /**
     * Validate publish year
     */
    public void validatePublishYear(Integer publishYear) {
        if (publishYear != null) {
            int currentYear = java.time.Year.now().getValue();
            if (publishYear < 1000 || publishYear > currentYear + 1) {
                throw new ValidationException("Invalid publish year", "PUBLISH_YEAR_INVALID");
            }
        }
    }

    /**
     * Validate language
     */
    public void validateLanguage(String language) {
        if (language != null && language.length() > 50) {
            throw new ValidationException("Language cannot exceed 50 characters", "LANGUAGE_TOO_LONG");
        }
    }

    /**
     * Check if book information is complete
     */
    public boolean isBookComplete(Book book) {
        return book.getIsbn() != null && !book.getIsbn().trim().isEmpty() &&
                book.getPublishYear() != null &&
                book.getPageCount() != null &&
                book.getLanguage() != null && !book.getLanguage().trim().isEmpty();
    }

    /**
     * Check if book can be updated
     */
    public boolean canBookBeUpdated(Book book) {
        // Business rule: Books can always be updated unless product is deleted
        return book.getProduct() == null || book.getProduct().getStatus() != Product.Status.DELETED;
    }

    /**
     * Validate all book fields
     */
    public void validateBookFields(String isbn, String description, Integer publishYear, Integer pageCount,
            String language) {
        validateIsbn(isbn);
        validatePublishYear(publishYear);
        validatePageCount(pageCount);
        validateLanguage(language);
    }
}