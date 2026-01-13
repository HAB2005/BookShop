package com.example.system_backend.user.application.service;

import com.example.system_backend.auth.domain.AuthValidationService;
import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.auth.repository.AuthProviderRepository;
import com.example.system_backend.common.exception.DuplicateResourceException;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.user.domain.UserValidationService;
import com.example.system_backend.user.dto.ChangePasswordRequest;
import com.example.system_backend.user.dto.CreateUserRequest;
import com.example.system_backend.user.dto.UpdateProfileRequest;
import com.example.system_backend.user.entity.User;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserCommandService handles ONLY User entity write operations. Pure CQRS - no
 * cross-domain orchestration. Uses domain validation services for all business
 * logic.
 */
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService userValidationService;
    private final AuthValidationService authValidationService;

    /**
     * Create a new user (User entity only)
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Use domain service for enum validation
        User.Role roleEnum = User.Role.parseRole(request.getRole());
        User.Status statusEnum = User.Status.parseStatus(request.getStatus());

        // Tạo user mới
        User user = new User();
        userValidationService.changeUserEmail(user, request.getEmail()); // Use domain service
        user.setFullName(request.getFullName());
        user.setRole(roleEnum);
        user.setStatus(statusEnum);

        return userRepository.save(user);
    }

    /**
     * Update existing user profile (User entity only)
     */
    public User updateUserProfile(Integer userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Kiểm tra email đã tồn tại chưa (nếu có thay đổi email)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
        }

        // Use domain service for profile update
        userValidationService.updateUserProfile(user, request.getEmail(), request.getFullName());

        return userRepository.save(user);
    }

    /**
     * Update user status (User entity only)
     */
    public void updateUserStatus(Integer userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Use domain service for status validation and change
        userValidationService.changeUserStatus(user, status);

        userRepository.save(user);
    }

    /**
     * Promote user to admin (business language)
     */
    public void promoteUserToAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.promoteUserToAdmin(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Demote admin to customer (business language)
     */
    public void demoteAdminToCustomer(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.demoteUserToCustomer(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Ban user (business language)
     */
    public void banUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.banUser(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Activate user (business language)
     */
    public void activateUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.activateUser(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Create AuthProvider for user (cross-domain operation)
     */
    public void createAuthProvider(Integer userId, String email, String password) {
        // Tạo AuthProvider cho LOCAL
        AuthProvider authProvider = new AuthProvider();
        authProvider.setUserId(userId);
        authProvider.setProvider(AuthProvider.Provider.LOCAL);
        authProvider.setProviderUserId(email);
        authProvider.setPasswordHash(passwordEncoder.encode(password));

        authProviderRepository.save(authProvider);
    }

    /**
     * Change user password (AuthProvider operation)
     */
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        // Use domain service for password validation
        authValidationService.validatePasswordConfirmation(request.getNewPassword(), request.getConfirmPassword());

        // Find LOCAL auth provider
        Optional<AuthProvider> authProviderOpt = authProviderRepository.findByUserIdAndProvider(
                userId, AuthProvider.Provider.LOCAL);

        if (authProviderOpt.isEmpty()) {
            throw new ValidationException("User does not have a local password", "NO_LOCAL_PASSWORD");
        }

        AuthProvider authProvider = authProviderOpt.get();

        // Verify current password using domain service
        if (!authValidationService.verifyAuthProviderPassword(authProvider, request.getCurrentPassword(),
                passwordEncoder)) {
            throw new ValidationException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }

        // Update password using domain service
        authValidationService.updateAuthProviderPassword(authProvider, request.getNewPassword(), passwordEncoder);
        authProviderRepository.save(authProvider);
    }

    /**
     * Reset user password to default (AuthProvider operation)
     */
    @Transactional
    public void resetUserPassword(Integer userId) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

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
            authProvider.setProviderUserId(user.getEmail());
        }

        // Reset password to default using domain service
        authValidationService.resetAuthProviderPassword(authProvider, passwordEncoder);
        authProviderRepository.save(authProvider);
    }
}
