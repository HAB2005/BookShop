package com.example.system_backend.product.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {
    
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;
    
    private String description;
    
    @Min(value = 1, message = "Publish year must be positive")
    private Integer publishYear;
    
    @Min(value = 1, message = "Page count must be positive")
    private Integer pageCount;
    
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
}