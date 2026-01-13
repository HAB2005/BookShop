package com.example.system_backend.auth.application.service;

import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.auth.repository.AuthProviderRepository;
import com.example.system_backend.common.exception.AuthenticationException;
import com.example.system_backend.user.entity.User;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AuthQueryService handles ONLY Auth-related read operations. Pure CQRS - no
 * validation logic, no cross-domain orchestration.
 */
@Service
@RequiredArgsConstructor
public class AuthQueryService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    /**
     * Find auth provider by provider type and provider user ID
     */
    public Optional<AuthProvider> findAuthProvider(AuthProvider.Provider provider, String providerUserId) {
        return authProviderRepository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    /**
     * Find auth provider by user ID and provider type
     */
    public Optional<AuthProvider> findAuthProviderByUserAndProvider(Integer userId, AuthProvider.Provider provider) {
        return authProviderRepository.findByUserIdAndProvider(userId, provider);
    }

    /**
     * Get user by ID - pure query
     */
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(AuthenticationException::invalidCredentials);
    }

    /**
     * Find user by email - pure query
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Check if email exists - pure query
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if user has local auth provider - pure query
     */
    public boolean hasLocalAuthProvider(Integer userId) {
        return authProviderRepository.findByUserIdAndProvider(userId, AuthProvider.Provider.LOCAL).isPresent();
    }
}
