package com.example.system_backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignCategoriesRequest {

    private List<Integer> categoryIds; // Can be empty list to remove all categories
}
