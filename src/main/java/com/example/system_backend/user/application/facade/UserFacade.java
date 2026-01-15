package com.example.system_backend.user.application.facade;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.user.application.service.UserCommandService;
import com.example.system_backend.user.application.service.UserQueryService;
import com.example.system_backend.user.dto.ChangePasswordRequest;
import com.example.system_backend.user.dto.CreateUserRequest;
import com.example.system_backend.user.dto.UpdateProfileRequest;
import com.example.system_backend.user.dto.UserListResponse;
import com.example.system_backend.user.dto.UserProfileResponse;
import com.example.system_backend.user.entity.Role;
import com.example.system_backend.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserFacade acts as a pure orchestrator for cross-domain operations.
 * Coordinates between User and Auth domains. This is the ONLY service that
 * should do cross-domain orchestration.
 */
@Service
@RequiredArgsConstructor
public class UserFacade {

    // Domain services
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    // Mappers for cross-domain operations
    private final UserMapper userMapper;

    // Query operations with cross-domain orchestration
    public UserProfileResponse getUserProfile(String identifier) {
        Role user = userQueryService.findUserByIdentifier(identifier);
        return userMapper.mapToUserProfileResponse(user);
    }

    public UserProfileResponse getUserProfileById(Integer userId) {
        Role user = userQueryService.getUserById(userId);
        return userMapper.mapToUserProfileResponse(user);
    }

    public UserProfileResponse getAdminUserProfile(Integer userId) {
        Role user = userQueryService.getUserById(userId);
        return userMapper.mapToAdminProfileResponse(user);
    }

    public PageResponse<UserListResponse> getAllUsers(int page, int size, String sortBy, String sortDir,
            String username, String email, String role, String status) {

        // Application logic: validate and set default values
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 100); // Max 100 items per page
        sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "createdAt";
        sortDir = (sortDir != null && sortDir.equalsIgnoreCase("asc")) ? "asc" : "desc";

        // Create sort object
        Sort sort = sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Application logic: parse enum values using domain methods
        com.example.system_backend.common.enums.UserRole roleEnum = null;
        com.example.system_backend.common.enums.UserStatus statusEnum = null;

        if (role != null && !role.isEmpty()) {
            roleEnum = com.example.system_backend.common.enums.UserRole.parseRole(role);
        }
        if (status != null && !status.isEmpty()) {
            statusEnum = com.example.system_backend.common.enums.UserStatus.parseStatus(status);
        }

        // Get users from UserQueryService (pure domain call)
        Page<Role> userPage = userQueryService.getUsersRaw(email, username, roleEnum, statusEnum, pageable);

        // Application logic: build PageResponse
        return PageResponse.<UserListResponse>builder()
                .content(userPage.getContent().stream()
                        .map(userMapper::mapToListResponse)
                        .toList())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }

    // Command operations with cross-domain orchestration
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        // Create user
        Role savedUser = userCommandService.createUser(request);

        // Create AuthProvider (cross-domain operation)
        userCommandService.createAuthProvider(savedUser.getUserId(), request.getEmail(), request.getPassword());

        return userMapper.mapToAdminProfileResponse(savedUser);
    }

    @Transactional
    public UserProfileResponse updateProfile(String identifier, UpdateProfileRequest request) {
        Role user = userQueryService.findUserByIdentifier(identifier);
        Role updatedUser = userCommandService.updateUserProfile(user.getUserId(), request);
        return userMapper.mapToUserProfileResponse(updatedUser);
    }

    @Transactional
    public UserProfileResponse adminUpdateUserProfile(Integer userId, UpdateProfileRequest request) {
        Role updatedUser = userCommandService.updateUserProfile(userId, request);
        return userMapper.mapToAdminProfileResponse(updatedUser);
    }

    public void updateUserStatus(Integer userId, String status) {
        // Pure delegation to UserCommandService
        userCommandService.updateUserStatus(userId, status);
    }

    @Transactional
    public void changePassword(String identifier, ChangePasswordRequest request) {
        Role user = userQueryService.findUserByIdentifier(identifier);
        userCommandService.changePassword(user.getUserId(), request);
    }

    public void resetUserPassword(Integer userId) {
        // Pure delegation to UserCommandService
        userCommandService.resetUserPassword(userId);
    }
}
