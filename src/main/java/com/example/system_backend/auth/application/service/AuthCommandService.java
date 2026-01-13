package com.example.system_backend.auth.application.service;

import com.example.system_backend.auth.domain.AuthValidationService;
import com.example.system_backend.auth.dto.GoogleUserInfo;
import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.auth.repository.AuthProviderRepository;
import com.example.system_backend.user.domain.UserValidationService;
import com.example.system_backend.user.entity.User;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthCommandService handles ONLY Auth-related write operations. Pure CQRS - no
 * cross-domain orchestration, uses domain validation services.
 */
@Service
@RequiredArgsConstructor
public class AuthCommandService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthValidationService authValidationService;
    private final UserValidationService userValidationService;

    /**
     * Create user with email - uses domain validation service
     */
    @Transactional
    public User createUser(String email, String fullName) {
        User user = new User();
        userValidationService.changeUserEmail(user, email); // Use user domain service
        user.setFullName(fullName);
        user.setRole(User.Role.customer);
        user.setStatus(User.Status.active);

        return userRepository.save(user);
    }

    /**
     * Create user without email (for phone registration)
     */
    @Transactional
    public User createUserWithoutEmail() {
        User user = new User();
        user.setRole(User.Role.customer);
        user.setStatus(User.Status.active);

        return userRepository.save(user);
    }

    /**
     * Update user from Google info - uses domain validation service
     */
    @Transactional
    public User updateUserFromGoogle(User user, GoogleUserInfo googleInfo) {
        if (googleInfo.getName() != null && !googleInfo.getName().isEmpty()) {
            userValidationService.updateUserProfile(user, null, googleInfo.getName()); // Use user domain service
            return userRepository.save(user);
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
