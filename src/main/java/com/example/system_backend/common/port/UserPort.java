package com.example.system_backend.common.port;

import com.example.system_backend.common.enums.UserRole;
import com.example.system_backend.common.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * UserPort interface - allows auth module to work with user data without
 * depending on user entity
 * This breaks the circular dependency between auth and user modules
 */
public interface UserPort {
    Integer getUserId();

    String getEmail();

    String getFullName();

    LocalDateTime getCreatedAt();

    UserRole getRole();

    UserStatus getStatus();
}
