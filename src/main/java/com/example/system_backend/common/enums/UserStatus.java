package com.example.system_backend.common.enums;

import com.example.system_backend.common.exception.ValidationException;

/**
 * User status enum - shared across modules
 */
public enum UserStatus {
    active, inactive, banned;

    /**
     * Parse status string with validation
     */
    public static UserStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty", "STATUS_EMPTY");
        }
        try {
            return UserStatus.valueOf(statusStr.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status. Must be 'active', 'inactive', or 'banned'", "INVALID_STATUS");
        }
    }
}
