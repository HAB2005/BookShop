package com.example.system_backend.common.exception;

import org.springframework.http.HttpStatus;

public class IntegrationException extends BaseException {

    public IntegrationException(String message) {
        super(message, "INTEGRATION_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public IntegrationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public IntegrationException(String message, Throwable cause) {
        super(message, "INTEGRATION_ERROR", HttpStatus.SERVICE_UNAVAILABLE, cause);
    }

    public IntegrationException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}