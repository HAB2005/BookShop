package com.example.system_backend.auth.domain;

import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.common.exception.AuthenticationException;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthValidationService encapsulates all authentication domain validation
 * rules.
 * Pure domain logic - no application concerns.
 * Focuses only on AuthProvider and authentication-specific logic.
 */
@Service
@RequiredArgsConstructor
public class AuthValidationService {

    /**
     * Validate password confirmation matches
     */
    public void validatePasswordConfirmation(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            throw new ValidationException("Password and confirmation cannot be null", "PASSWORD_NULL");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match", "PASSWORD_MISMATCH");
        }
    }

    /**
     * Validate user status for authentication
     */
    public void validateUserStatus(User user) {
        if (user == null) {
            throw AuthenticationException.invalidCredentials();
        }
        if (user.getStatus() != User.Status.active) {
            throw AuthenticationException.accountDisabled(user.getStatus().name());
        }
    }

    /**
     * Validate auth provider exists and is valid
     */
    public void validateAuthProvider(AuthProvider provider) {
        if (provider == null) {
            throw AuthenticationException.invalidCredentials();
        }
    }

    /**
     * Validate password strength (domain rule)
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty", "PASSWORD_EMPTY");
        }
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters", "PASSWORD_TOO_SHORT");
        }
    }

    /**
     * Validate email format (domain rule)
     */
    public void validateEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty", "EMAIL_EMPTY");
        }
        if (!email.contains("@")) {
            throw new ValidationException("Invalid email format", "EMAIL_INVALID");
        }
    }

    /**
     * Validate phone format (domain rule)
     */
    public void validatePhoneFormat(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone cannot be empty", "PHONE_EMPTY");
        }
        // Basic phone validation - can be enhanced
        if (phone.length() < 10) {
            throw new ValidationException("Phone number must be at least 10 digits", "PHONE_TOO_SHORT");
        }
    }

    // ===== AUTH PROVIDER DOMAIN LOGIC =====

    /**
     * Update auth provider password with validation
     */
    public void updateAuthProviderPassword(AuthProvider authProvider, String newPassword,
            PasswordEncoder passwordEncoder) {
        if (authProvider.getProvider() != AuthProvider.Provider.LOCAL) {
            throw new ValidationException("Cannot set password for non-local provider",
                    "INVALID_PROVIDER_FOR_PASSWORD");
        }
        validatePasswordStrength(newPassword);
        authProvider.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    /**
     * Verify auth provider password
     */
    public boolean verifyAuthProviderPassword(AuthProvider authProvider, String rawPassword,
            PasswordEncoder passwordEncoder) {
        if (authProvider.getProvider() != AuthProvider.Provider.LOCAL) {
            return false;
        }
        if (authProvider.getPasswordHash() == null || rawPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, authProvider.getPasswordHash());
    }

    /**
     * Change auth provider type with validation
     */
    public void changeAuthProvider(AuthProvider authProvider, AuthProvider.Provider newProvider,
            String newProviderUserId) {
        if (newProvider == null) {
            throw new ValidationException("Provider cannot be null", "PROVIDER_NULL");
        }
        if (newProviderUserId == null || newProviderUserId.trim().isEmpty()) {
            throw new ValidationException("Provider user ID cannot be empty", "PROVIDER_USER_ID_EMPTY");
        }

        authProvider.setProvider(newProvider);
        authProvider.setProviderUserId(newProviderUserId);

        // Clear password hash if changing from LOCAL to other providers
        if (newProvider != AuthProvider.Provider.LOCAL) {
            authProvider.setPasswordHash(null);
        }
    }

    /**
     * Reset auth provider password to default
     */
    public void resetAuthProviderPassword(AuthProvider authProvider, PasswordEncoder passwordEncoder) {
        if (authProvider.getProvider() != AuthProvider.Provider.LOCAL) {
            throw new ValidationException("Cannot reset password for non-local provider",
                    "INVALID_PROVIDER_FOR_PASSWORD");
        }
        authProvider.setPasswordHash(passwordEncoder.encode("123456"));
    }

    /**
     * Check if auth provider password is set
     */
    public boolean isAuthProviderPasswordSet(AuthProvider authProvider) {
        return authProvider.getProvider() == AuthProvider.Provider.LOCAL &&
                authProvider.getPasswordHash() != null &&
                !authProvider.getPasswordHash().isEmpty();
    }
}