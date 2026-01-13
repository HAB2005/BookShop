package com.example.system_backend.user.mapper;

import com.example.system_backend.user.dto.UserListResponse;
import com.example.system_backend.user.dto.UserProfileResponse;
import com.example.system_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * UserMapper handles mapping between User entities and response DTOs. Separates
 * mapping logic from business logic in services.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    /**
     * Map User entity to UserProfileResponse DTO (for regular users)
     *
     * @param user the User entity
     * @return UserProfileResponse DTO
     */
    public UserProfileResponse mapToUserProfileResponse(User user) {
        // Determine display name (email or user_id)
        String displayName = user.getEmail();
        if (displayName == null) {
            displayName = "user_" + user.getUserId();
        }

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(displayName)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(null) // User thường không thấy status
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Map User entity to UserProfileResponse DTO (for admin users)
     *
     * @param user the User entity
     * @return UserProfileResponse DTO with status visible
     */
    public UserProfileResponse mapToAdminProfileResponse(User user) {
        // Determine display name (email or user_id)
        String displayName = user.getEmail();
        if (displayName == null) {
            displayName = "user_" + user.getUserId();
        }

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(displayName)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name()) // Admin thấy status
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Map User entity to UserListResponse DTO
     *
     * @param user the User entity
     * @return UserListResponse DTO
     */
    public UserListResponse mapToListResponse(User user) {
        // Determine display name (email or user_id)
        String displayName = user.getEmail();
        if (displayName == null) {
            displayName = "user_" + user.getUserId();
        }

        return UserListResponse.builder()
                .userId(user.getUserId())
                .username(displayName)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
