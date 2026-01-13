package com.example.system_backend.common.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends SecurityException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid username or password");
    }

    public static AuthenticationException accountDisabled(String status) {
        return new AuthenticationException(
                "Account is " + status + ". Please contact administrator.",
                "ACCOUNT_DISABLED");
    }

    public static AuthenticationException userNotFound() {
        return new AuthenticationException("User not found");
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Token has expired");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid token");
    }

    public static AuthenticationException invalidGoogleToken() {
        return new AuthenticationException("Invalid Google ID token", "INVALID_GOOGLE_TOKEN");
    }

    public static AuthenticationException googleAuthFailed() {
        return new AuthenticationException("Google authentication failed", "GOOGLE_AUTH_FAILED");
    }

    public static AuthenticationException otpExpired() {
        return new AuthenticationException("OTP has expired", "OTP_EXPIRED");
    }

    public static AuthenticationException invalidOtp() {
        return new AuthenticationException("Invalid OTP code", "INVALID_OTP");
    }
}
