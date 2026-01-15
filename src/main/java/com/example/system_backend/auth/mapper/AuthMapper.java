package com.example.system_backend.auth.mapper;

import com.example.system_backend.auth.dto.AuthResponse;
import com.example.system_backend.common.port.UserPort;
import org.springframework.stereotype.Component;

/**
 * AuthMapper handles pure mapping from UserPort to AuthResponse DTOs.
 * No business logic, no side effects.
 * Uses UserPort interface to avoid dependency on User entity.
 */
@Component
public class AuthMapper {

    /**
     * Map UserPort to AuthResponse DTO (without token)
     */
    public AuthResponse mapToAuthResponse(UserPort user) {
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
     * Map UserPort to AuthResponse DTO with token
     */
    public AuthResponse mapToAuthResponseWithToken(UserPort user, String token) {
        AuthResponse response = mapToAuthResponse(user);
        response.token = token;
        return response;
    }
}