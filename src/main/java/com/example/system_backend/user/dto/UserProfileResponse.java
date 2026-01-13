package com.example.system_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String status; // Sẽ được set null cho user thường
    private LocalDateTime createdAt;
}