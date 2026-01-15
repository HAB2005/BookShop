package com.example.system_backend.user.domain;

import com.example.system_backend.common.enums.UserRole;
import com.example.system_backend.common.enums.UserStatus;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UserValidationService encapsulates all user domain validation rules. Pure
 * domain logic - no application concerns.
 */
@Service
@RequiredArgsConstructor
public class UserValidationService {

    /**
     * Validate email format
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
     * Change user email with validation
     */
    public void changeUserEmail(Role user, String newEmail) {
        validateEmailFormat(newEmail);
        user.setEmail(newEmail.trim().toLowerCase());
    }

    /**
     * Update user profile with validation
     */
    public void updateUserProfile(Role user, String email, String fullName) {
        if (email != null) {
            changeUserEmail(user, email);
        }
        if (fullName != null) {
            user.setFullName(fullName.trim());
        }
    }

    /**
     * Change user status with validation
     */
    public void changeUserStatus(Role user, UserStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("Status cannot be null", "INVALID_STATUS");
        }
        user.setStatus(newStatus);
    }

    /**
     * Change user status from string with validation
     */
    public void changeUserStatus(Role user, String statusString) {
        UserStatus newStatus = UserStatus.parseStatus(statusString);
        changeUserStatus(user, newStatus);
    }

    /**
     * Change user role with validation
     */
    public void changeUserRole(Role user, UserRole newRole) {
        if (newRole == null) {
            throw new ValidationException("Role cannot be null", "ROLE_NULL");
        }
        user.setRole(newRole);
    }

    /**
     * Promote user to admin
     */
    public void promoteUserToAdmin(Role user) {
        user.setRole(UserRole.admin);
    }

    /**
     * Demote admin to customer
     */
    public void demoteUserToCustomer(Role user) {
        user.setRole(UserRole.customer);
    }

    /**
     * Activate user
     */
    public void activateUser(Role user) {
        user.setStatus(UserStatus.active);
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(Role user) {
        user.setStatus(UserStatus.inactive);
    }

    /**
     * Ban user
     */
    public void banUser(Role user) {
        user.setStatus(UserStatus.banned);
    }

    /**
     * Check if user is active
     */
    public boolean isUserActive(Role user) {
        return user.getStatus() == UserStatus.active;
    }

    /**
     * Check if user is admin
     */
    public boolean isUserAdmin(Role user) {
        return user.getRole() == UserRole.admin;
    }
}
