package com.example.system_backend.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE", HttpStatus.CONFLICT);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue),
                "DUPLICATE_RESOURCE", HttpStatus.CONFLICT);
    }

    public static DuplicateResourceException usernameExists(String username) {
        return new DuplicateResourceException("User", "username", username);
    }

    public static DuplicateResourceException emailExists(String email) {
        return new DuplicateResourceException("User", "email", email);
    }
}
