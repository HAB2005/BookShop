package com.example.system_backend.common.port;

/**
 * Port interface for password management operations.
 * This allows user module to request password operations without depending on auth module.
 */
public interface PasswordManagementPort {
    
    /**
     * Create authentication provider with password for a user
     */
    void createAuthProviderWithPassword(Integer userId, String email, String password);
    
    /**
     * Change user password
     */
    void changePassword(Integer userId, String currentPassword, String newPassword, String confirmPassword);
    
    /**
     * Reset user password to default
     */
    void resetPassword(Integer userId, String email);
}
