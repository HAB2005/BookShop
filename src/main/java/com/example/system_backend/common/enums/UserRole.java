package com.example.system_backend.common.enums;

import com.example.system_backend.common.exception.ValidationException;

/**
 * User role enum - shared across modules
 */
public enum UserRole {
    customer, admin;

    /**
     * Parse role string with validation
     */
    public static UserRole parseRole(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            throw new ValidationException("Role cannot be empty", "ROLE_EMPTY");
        }
        try {
            return UserRole.valueOf(roleStr.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role. Must be 'customer' or 'admin'", "INVALID_ROLE");
        }
    }
}
