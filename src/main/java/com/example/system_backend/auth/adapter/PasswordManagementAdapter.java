package com.example.system_backend.auth.adapter;

import com.example.system_backend.auth.domain.AuthValidationService;
import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.auth.repository.AuthProviderRepository;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.common.port.PasswordManagementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adapter that implements PasswordManagementPort using auth module services.
 * This allows other modules to use password operations without direct dependency on auth module.
 */
@Component
@RequiredArgsConstructor
public class PasswordManagementAdapter implements PasswordManagementPort {

    private final AuthProviderRepository authProviderRepository;
    private final AuthValidationService authValidationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void createAuthProviderWithPassword(Integer userId, String email, String password) {
        AuthProvider authProvider = new AuthProvider();
        authProvider.setUserId(userId);
        authProvider.setProvider(AuthProvider.Provider.LOCAL);
        authProvider.setProviderUserId(email);
        authProvider.setPasswordHash(passwordEncoder.encode(password));
        
        authProviderRepository.save(authProvider);
    }

    @Override
    @Transactional
    public void changePassword(Integer userId, String currentPassword, String newPassword, String confirmPassword) {
        // Validate password confirmation
        authValidationService.validatePasswordConfirmation(newPassword, confirmPassword);

        // Find LOCAL auth provider
        Optional<AuthProvider> authProviderOpt = authProviderRepository.findByUserIdAndProvider(
                userId, AuthProvider.Provider.LOCAL);

        if (authProviderOpt.isEmpty()) {
            throw new ValidationException("User does not have a local password", "NO_LOCAL_PASSWORD");
        }

        AuthProvider authProvider = authProviderOpt.get();

        // Verify current password
        if (!authValidationService.verifyAuthProviderPassword(authProvider, currentPassword, passwordEncoder)) {
            throw new ValidationException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }

        // Update password
        authValidationService.updateAuthProviderPassword(authProvider, newPassword, passwordEncoder);
        authProviderRepository.save(authProvider);
    }

    @Override
    @Transactional
    public void resetPassword(Integer userId, String email) {
        // Find or create LOCAL auth provider
        Optional<AuthProvider> authProviderOpt = authProviderRepository.findByUserIdAndProvider(
                userId, AuthProvider.Provider.LOCAL);

        AuthProvider authProvider;
        if (authProviderOpt.isPresent()) {
            authProvider = authProviderOpt.get();
        } else {
            // Create new LOCAL auth provider if doesn't exist
            authProvider = new AuthProvider();
            authProvider.setUserId(userId);
            authProvider.setProvider(AuthProvider.Provider.LOCAL);
            authProvider.setProviderUserId(email);
        }

        // Reset password to default
        authValidationService.resetAuthProviderPassword(authProvider, passwordEncoder);
        authProviderRepository.save(authProvider);
    }
}
