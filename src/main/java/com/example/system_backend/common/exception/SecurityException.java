package com.example.system_backend.common.exception;

import org.springframework.http.HttpStatus;

public class SecurityException extends BaseException {

    public SecurityException(String message) {
        super(message, "SECURITY_ERROR", HttpStatus.UNAUTHORIZED);
    }

    public SecurityException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }

    public SecurityException(String message, HttpStatus httpStatus) {
        super(message, "SECURITY_ERROR", httpStatus);
    }

    public SecurityException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}