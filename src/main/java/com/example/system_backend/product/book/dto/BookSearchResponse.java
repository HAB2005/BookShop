package com.example.system_backend.product.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponse {
    
    // Essential product information for list view
    private Integer productId;
    private String name;
    private BigDecimal price;
    
    // Essential book information for list view
    private String isbn;
    private Integer publishYear;
    private String language;
}