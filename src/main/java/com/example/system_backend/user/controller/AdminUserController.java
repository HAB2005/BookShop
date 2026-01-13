package com.example.system_backend.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.system_backend.common.response.PageResponse;
import com.example.system_backend.user.application.facade.UserFacade;
import com.example.system_backend.user.dto.CreateUserRequest;
import com.example.system_backend.user.dto.UpdateProfileRequest;
import com.example.system_backend.user.dto.UserListResponse;
import com.example.system_backend.user.dto.UserProfileResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserFacade userFacade;

    @GetMapping
    public ResponseEntity<PageResponse<UserListResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        PageResponse<UserListResponse> users = userFacade.getAllUsers(
                page, size, sortBy, sortDir, username, email, role, status);

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserProfileResponse createdUser = userFacade.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserDetail(@PathVariable Integer userId) {
        UserProfileResponse user = userFacade.getAdminUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam String status) {

        userFacade.updateUserStatus(userId, status);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User status updated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse updatedUser = userFacade.adminUpdateUserProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetUserPassword(
            @PathVariable Integer userId) {

        userFacade.resetUserPassword(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User password reset to default successfully");
        return ResponseEntity.ok(response);
    }
}
