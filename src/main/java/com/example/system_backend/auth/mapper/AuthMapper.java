package com.example.system_backend.auth.mapper;

import com.example.system_backend.auth.dto.AuthResponse;
import com.example.system_backend.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * AuthMapper handles pure mapping from User entities to AuthResponse DTOs.
 * No business logic, no side effects.
 */
@Component
public class AuthMapper {

    /**
     * Map User entity to AuthResponse DTO (without token)
     */
    public AuthResponse mapToAuthResponse(User user) {
        String identifier = user.getEmail();
        if (identifier == null) {
            identifier = "user_" + user.getUserId();
        }

        AuthResponse response = new AuthResponse();
        response.userId = user.getUserId();
        response.username = identifier;
        response.fullName = user.getFullName();
        response.role = user.getRole().name();
        // Token will be set by AuthFacade
        return response;
    }

    /**
     * Map User entity to AuthResponse DTO with token
     */
    public AuthResponse mapToAuthResponseWithToken(User user, String token) {
        AuthResponse response = mapToAuthResponse(user);
        response.token = token;
        return response;
    }
}