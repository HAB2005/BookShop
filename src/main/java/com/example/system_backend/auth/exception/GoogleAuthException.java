package com.example.system_backend.auth.exception;

import com.example.system_backend.common.exception.AuthenticationException;

/**
 * Exception thrown when Google authentication fails
 */
public class GoogleAuthException extends AuthenticationException {
    
    public GoogleAuthException(String message) {
        super(message, "GOOGLE_AUTH_ERROR");
    }
}
