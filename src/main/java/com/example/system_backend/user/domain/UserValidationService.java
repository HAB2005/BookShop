package com.example.system_backend.user.domain;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.user.entity.User;
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
    public void changeUserEmail(User user, String newEmail) {
        validateEmailFormat(newEmail);
        user.setEmail(newEmail.trim().toLowerCase());
    }

    /**
     * Update user profile with validation
     */
    public void updateUserProfile(User user, String email, String fullName) {
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
    public void changeUserStatus(User user, User.Status newStatus) {
        if (newStatus == null) {
            throw new ValidationException("Status cannot be null", "INVALID_STATUS");
        }
        user.setStatus(newStatus);
    }

    /**
     * Change user status from string with validation
     */
    public void changeUserStatus(User user, String statusString) {
        User.Status newStatus = User.Status.parseStatus(statusString);
        changeUserStatus(user, newStatus);
    }

    /**
     * Change user role with validation
     */
    public void changeUserRole(User user, User.Role newRole) {
        if (newRole == null) {
            throw new ValidationException("Role cannot be null", "ROLE_NULL");
        }
        user.setRole(newRole);
    }

    /**
     * Promote user to admin
     */
    public void promoteUserToAdmin(User user) {
        user.setRole(User.Role.admin);
    }

    /**
     * Demote admin to customer
     */
    public void demoteUserToCustomer(User user) {
        user.setRole(User.Role.customer);
    }

    /**
     * Activate user
     */
    public void activateUser(User user) {
        user.setStatus(User.Status.active);
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(User user) {
        user.setStatus(User.Status.inactive);
    }

    /**
     * Ban user
     */
    public void banUser(User user) {
        user.setStatus(User.Status.banned);
    }

    /**
     * Check if user is active
     */
    public boolean isUserActive(User user) {
        return user.getStatus() == User.Status.active;
    }

    /**
     * Check if user is admin
     */
    public boolean isUserAdmin(User user) {
        return user.getRole() == User.Role.admin;
    }
}
