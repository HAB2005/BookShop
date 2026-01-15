package com.example.system_backend.common.enums;

import com.example.system_backend.common.exception.ValidationException;

/**
 * Category status enum - shared across layers to avoid DTO depending on Entity
 */
public enum CategoryStatus {
    ACTIVE, INACTIVE;

    /**
     * Parse status string with validation
     */
    public static CategoryStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty", "STATUS_EMPTY");
        }
        try {
            return CategoryStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status. Must be 'ACTIVE' or 'INACTIVE'", "INVALID_STATUS");
        }
    }
}
