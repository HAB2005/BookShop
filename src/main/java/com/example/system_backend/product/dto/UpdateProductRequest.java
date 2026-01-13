package com.example.system_backend.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import com.example.system_backend.product.book.dto.UpdateBookRequest;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    // Category IDs to assign to this product (optional - null means no change)
    private List<Integer> categoryIds;

    // Book information (optional - only if updating a book product)
    @Valid
    private UpdateBookRequest book;
}
