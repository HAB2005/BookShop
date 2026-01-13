package com.example.system_backend.product.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImageRequest {
    private Boolean isPrimary;
    private Integer sortOrder;
}