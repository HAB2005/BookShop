package com.example.system_backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Size(max = 100, message = "Password must not exceed 100 characters")
    private String password;

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @NotNull(message = "Role is required")
    private String role; // "customer" or "admin"

    private String status = "active"; // "active", "inactive", "banned"
}