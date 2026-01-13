package com.example.system_backend.product.category.domain;

/**
 * Enumeration of category validation error codes. Provides consistent error
 * codes for category validation failures.
 */
public enum CategoryValidationError {

    // Slug validation errors
    SLUG_EXISTS("Category slug already exists"),
    SLUG_NULL_OR_EMPTY("Slug cannot be null or empty"),
    // Parent validation errors
    PARENT_INACTIVE("Parent category must be active"),
    PARENT_NOT_FOUND("Parent category not found"),
    PARENT_ID_NULL("Parent ID cannot be null"),
    // Circular reference errors
    CIRCULAR_REFERENCE("Cannot set descendant as parent"),
    SELF_REFERENCE("Category cannot be its own parent"),
    // Deactivation errors
    HAS_ACTIVE_CHILDREN("Cannot deactivate category with active children"),
    // General validation errors
    CATEGORY_ID_NULL("Category ID cannot be null");

    private final String message;

    CategoryValidationError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return this.name();
    }
}
