package com.example.system_backend.user.controller;

import com.example.system_backend.user.application.facade.UserFacade;
import com.example.system_backend.user.dto.ChangePasswordRequest;
import com.example.system_backend.user.dto.UpdateProfileRequest;
import com.example.system_backend.user.dto.UserProfileResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserProfileResponse profile = userFacade.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfileById(@PathVariable Integer userId) {
        UserProfileResponse profile = userFacade.getUserProfileById(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserProfileResponse updatedProfile = userFacade.updateProfile(username, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userFacade.changePassword(username, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}
