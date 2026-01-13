package com.example.system_backend.user.application.service;

import com.example.system_backend.common.exception.ResourceNotFoundException;
import com.example.system_backend.user.entity.User;
import com.example.system_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UserQueryService handles ONLY User entity read operations. Pure CQRS - no
 * cross-domain orchestration, no application logic.
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * Find user by identifier (email or user_id format)
     */
    public User findUserByIdentifier(String identifier) {
        // Try email first
        Optional<User> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // If identifier starts with "user_", extract userId
        if (identifier.startsWith("user_")) {
            try {
                Integer userId = Integer.parseInt(identifier.substring(5));
                return userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", identifier));
            } catch (NumberFormatException e) {
                // Fall through to exception
            }
        }

        throw new ResourceNotFoundException("User", "identifier", identifier);
    }

    /**
     * Get single user by ID (User entity only)
     */
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Check if user exists by ID
     */
    public boolean existsById(Integer userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get paginated list of users with filters (User entity only). Pure domain
     * query - no application concerns.
     */
    public Page<User> getUsersRaw(String email, String username, User.Role role, User.Status status,
            Pageable pageable) {
        return userRepository.findUsersWithFilters(email, username, role, status, pageable);
    }
}
