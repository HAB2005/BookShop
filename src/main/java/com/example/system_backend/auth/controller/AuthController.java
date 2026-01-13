package com.example.system_backend.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.system_backend.auth.application.facade.AuthFacade;
import com.example.system_backend.auth.dto.AuthResponse;
import com.example.system_backend.auth.dto.EmailLoginRequest;
import com.example.system_backend.auth.dto.EmailRegisterRequest;
import com.example.system_backend.auth.dto.GoogleLoginRequest;
import com.example.system_backend.auth.dto.PhoneLoginRequest;
import com.example.system_backend.auth.dto.OtpVerifyRequest;
import com.example.system_backend.auth.dto.SetPasswordWithEmailRequest;
import com.example.system_backend.common.security.TokenBlacklistService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthFacade authFacade;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthFacade authFacade, TokenBlacklistService tokenBlacklistService) {
        this.authFacade = authFacade;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // ==================== EMAIL/PASSWORD AUTHENTICATION ====================

    @PostMapping("/email/register")
    public AuthResponse registerWithEmail(@Valid @RequestBody EmailRegisterRequest request) {
        return authFacade.registerWithEmail(request);
    }

    @PostMapping("/email/login")
    public AuthResponse loginWithEmail(@Valid @RequestBody EmailLoginRequest request) {
        return authFacade.loginWithEmail(request);
    }

    @PostMapping("/email/set-password")
    public AuthResponse setPasswordForExistingUser(@Valid @RequestBody SetPasswordWithEmailRequest request) {
        return authFacade.setPasswordForExistingUser(request.getEmail(), request);
    }

    // ==================== GOOGLE OAUTH AUTHENTICATION ====================

    @PostMapping("/google/login")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authFacade.loginWithGoogle(request);
    }

    // ==================== PHONE OTP AUTHENTICATION ====================

    @PostMapping("/phone/send-otp")
    public ResponseEntity<Map<String, String>> sendPhoneOtp(@Valid @RequestBody PhoneLoginRequest request) {
        authFacade.sendPhoneOtp(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent successfully");
        response.put("phone", request.getPhone());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/phone/verify-otp")
    public AuthResponse loginWithPhone(@Valid @RequestBody OtpVerifyRequest request) {
        return authFacade.loginWithPhone(request);
    }

    // ==================== COMMON ====================

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid token");
        return ResponseEntity.badRequest().body(response);
    }
}