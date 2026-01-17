package com.example.system_backend.auth.application.service;

import com.example.system_backend.auth.domain.AuthValidationService;
import com.example.system_backend.auth.dto.GoogleUserInfo;
import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.auth.repository.AuthProviderRepository;
import com.example.system_backend.common.port.UserCommandPort;
import com.example.system_backend.common.port.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthCommandService handles ONLY Auth-related write operations. Pure CQRS - no
 * cross-domain orchestration, uses domain validation services.
 * Uses UserCommandPort to avoid direct dependency on user module.
 */
@Service
@RequiredArgsConstructor
public class AuthCommandService {

    private final UserCommandPort userCommandPort;
    private final AuthProviderRepository authProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthValidationService authValidationService;

    /**
     * Create user with email - delegates to user module via port
     */
    @Transactional
    public UserPort createUser(String email, String fullName) {
        return userCommandPort.createUserWithEmail(email, fullName);
    }

    /**
     * Create user without email (for phone registration) - delegates to user module
     * via port
     */
    @Transactional
    public UserPort createUserWithoutEmail() {
        return userCommandPort.createUserWithoutEmail();
    }

    /**
     * Update user from Google info - delegates to user module via port
     */
    @Transactional
    public UserPort updateUserFromGoogle(UserPort user, GoogleUserInfo googleInfo) {
        if (googleInfo.getName() != null && !googleInfo.getName().isEmpty()) {
            return userCommandPort.updateUserProfile(user, null, googleInfo.getName());
        }
        return user;
    }

    /**
     * Create local auth provider - uses domain validation service
     */
    @Transactional
    public AuthProvider createLocalAuthProvider(Integer userId, String email, String password) {
        AuthProvider authProvider = new AuthProvider();
        authProvider.setUserId(userId);
        authProvider.setProvider(AuthProvider.Provider.LOCAL);
        authProvider.setProviderUserId(email);
        authValidationService.updateAuthProviderPassword(authProvider, password, passwordEncoder); // Use domain service

        return authProviderRepository.save(authProvider);
    }

    /**
     * Create Google auth provider - uses domain validation service
     */
    @Transactional
    public AuthProvider createGoogleAuthProvider(Integer userId, String email) {
        AuthProvider authProvider = new AuthProvider();
        authProvider.setUserId(userId);
        authValidationService.changeAuthProvider(authProvider, AuthProvider.Provider.GOOGLE, email); // Use domain
        // service

        return authProviderRepository.save(authProvider);
    }

    /**
     * Create phone auth provider - uses domain validation service
     */
    @Transactional
    public AuthProvider createPhoneAuthProvider(Integer userId, String phone) {
        AuthProvider authProvider = new AuthProvider();
        authProvider.setUserId(userId);
        authValidationService.changeAuthProvider(authProvider, AuthProvider.Provider.PHONE, phone); // Use domain
        // service

        return authProviderRepository.save(authProvider);
    }

    /**
     * Update auth provider password - uses domain validation service
     */
    @Transactional
    public void updateAuthProviderPassword(AuthProvider authProvider, String newPassword) {
        authValidationService.updateAuthProviderPassword(authProvider, newPassword, passwordEncoder); // Use domain
        // service
        authProviderRepository.save(authProvider);
    }

    /**
     * Reset auth provider password to default - uses domain validation service
     */
    @Transactional
    public void resetAuthProviderPassword(AuthProvider authProvider) {
        authValidationService.resetAuthProviderPassword(authProvider, passwordEncoder); // Use domain service
        authProviderRepository.save(authProvider);
    }
}
