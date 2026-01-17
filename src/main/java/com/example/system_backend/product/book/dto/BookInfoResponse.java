package com.example.system_backend.product.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoResponse {

    private Integer bookId;
    private String isbn;
    private String description;
    private Integer publishYear;
    private Integer pageCount;
    private String language;
}