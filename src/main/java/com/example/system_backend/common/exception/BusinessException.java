package com.example.system_backend.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST, cause);
    }
}
