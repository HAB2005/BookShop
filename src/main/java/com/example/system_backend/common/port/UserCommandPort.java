package com.example.system_backend.common.port;

/**
 * UserCommandPort interface - allows auth module to create users without
 * depending on UserRepository
 * This breaks the circular dependency between auth and user modules
 */
public interface UserCommandPort {
    /**
     * Create a new user with email
     */
    UserPort createUserWithEmail(String email, String fullName);

    /**
     * Create a new user without email (for phone registration)
     */
    UserPort createUserWithoutEmail();

    /**
     * Update user profile
     */
    UserPort updateUserProfile(UserPort user, String email, String fullName);

    /**
     * Save user
     */
    UserPort saveUser(UserPort user);
}
