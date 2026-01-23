package com.example.system_backend.common.util;

import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.common.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class for authentication-related operations
 * Helps controllers extract user information without violating layer boundaries
 */
@Component
@RequiredArgsConstructor
public class AuthenticationUtil {
    
    private final JwtService jwtService;
    
    /**
     * Extract user ID from JWT token in request
     */
    public Integer getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractClaim(token, claims -> claims.get("userId", Integer.class));
        }
        throw new ValidationException("No valid JWT token found", "INVALID_TOKEN");
    }
}