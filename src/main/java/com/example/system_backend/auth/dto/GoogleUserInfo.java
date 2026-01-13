package com.example.system_backend.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO representing Google user information from OAuth response
 */
@Data
@Builder
public class GoogleUserInfo {
    private String googleId;
    private String email;
    private String name;
    private String picture;
    private boolean emailVerified;
}