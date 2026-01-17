package com.example.system_backend.user.application.service;

import com.example.system_backend.common.enums.UserRole;
import com.example.system_backend.common.enums.UserStatus;
import com.example.system_backend.common.exception.DuplicateResourceException;
import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.common.port.PasswordManagementPort;
import com.example.system_backend.user.domain.UserValidationService;
import com.example.system_backend.user.dto.ChangePasswordRequest;
import com.example.system_backend.user.dto.CreateUserRequest;
import com.example.system_backend.user.dto.UpdateProfileRequest;
import com.example.system_backend.user.entity.Role;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserCommandService handles ONLY User entity write operations. Pure CQRS - no
 * cross-domain orchestration. Uses domain validation services for all business
 * logic. Uses PasswordManagementPort for password operations without depending on auth module.
 */
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordManagementPort passwordManagementPort;

    /**
     * Create a new user (User entity only)
     */
    @Transactional
    public Role createUser(CreateUserRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Use domain service for enum validation
        UserRole roleEnum = UserRole.parseRole(request.getRole());
        UserStatus statusEnum = UserStatus.parseStatus(request.getStatus());

        // Tạo user mới
        Role user = new Role();
        userValidationService.changeUserEmail(user, request.getEmail()); // Use domain service
        user.setFullName(request.getFullName());
        user.setRole(roleEnum);
        user.setStatus(statusEnum);

        return userRepository.save(user);
    }

    /**
     * Update existing user profile (User entity only)
     */
    public Role updateUserProfile(Integer userId, UpdateProfileRequest request) {
        Role user = userRepository.findById(userId)
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
        Role user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Use domain service for status validation and change
        userValidationService.changeUserStatus(user, status);

        userRepository.save(user);
    }

    /**
     * Promote user to admin (business language)
     */
    public void promoteUserToAdmin(Integer userId) {
        Role user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.promoteUserToAdmin(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Demote admin to customer (business language)
     */
    public void demoteAdminToCustomer(Integer userId) {
        Role user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.demoteUserToCustomer(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Ban user (business language)
     */
    public void banUser(Integer userId) {
        Role user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.banUser(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Activate user (business language)
     */
    public void activateUser(Integer userId) {
        Role user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userValidationService.activateUser(user); // Use domain service
        userRepository.save(user);
    }

    /**
     * Create AuthProvider for user (delegates to auth module via port)
     */
    public void createAuthProvider(Integer userId, String email, String password) {
        passwordManagementPort.createAuthProviderWithPassword(userId, email, password);
    }

    /**
     * Change user password (delegates to auth module via port)
     */
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        passwordManagementPort.changePassword(
            userId, 
            request.getCurrentPassword(), 
            request.getNewPassword(), 
            request.getConfirmPassword()
        );
    }

    /**
     * Reset user password to default (delegates to auth module via port)
     */
    @Transactional
    public void resetUserPassword(Integer userId) {
        // Find user to get email
        Role user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        passwordManagementPort.resetPassword(userId, user.getEmail());
    }
}
