package com.example.system_backend.product.category.dto;

import com.example.system_backend.common.enums.CategoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryStatusRequest {

    @NotNull(message = "Status is required")
    private CategoryStatus status;
}
