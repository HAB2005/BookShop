package com.example.system_backend.auth.application.facade;

import com.example.system_backend.auth.application.service.AuthCommandService;
import com.example.system_backend.auth.application.service.AuthQueryService;
import com.example.system_backend.auth.application.service.GoogleAuthService;
import com.example.system_backend.auth.domain.AuthValidationService;
import com.example.system_backend.auth.dto.AuthResponse;
import com.example.system_backend.auth.dto.EmailLoginRequest;
import com.example.system_backend.auth.dto.EmailRegisterRequest;
import com.example.system_backend.auth.dto.GoogleLoginRequest;
import com.example.system_backend.auth.dto.GoogleUserInfo;
import com.example.system_backend.auth.dto.OtpVerifyRequest;
import com.example.system_backend.auth.dto.PhoneLoginRequest;
import com.example.system_backend.auth.dto.SetPasswordWithEmailRequest;
import com.example.system_backend.auth.entity.AuthProvider;
import com.example.system_backend.auth.mapper.AuthMapper;
import com.example.system_backend.common.exception.AuthenticationException;
import com.example.system_backend.common.exception.DuplicateResourceException;
import com.example.system_backend.common.exception.ValidationException;
import com.example.system_backend.common.port.UserPort;
import com.example.system_backend.common.security.JwtService;
import com.example.system_backend.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * AuthFacade orchestrates cross-domain operations between Auth, User, and OTP
 * domains. Pure orchestration - all validation logic moved to
 * AuthValidationService. Handles complex authentication workflows and
 * coordinates multiple services.
 * Uses UserPort to avoid direct dependency on User entity.
 */
@Service
@RequiredArgsConstructor
public class AuthFacade {

    // Domain services
    private final AuthValidationService authValidationService;
    private final AuthQueryService authQueryService;
    private final AuthCommandService authCommandService;
    private final GoogleAuthService googleAuthService;
    private final OtpService otpService;

    // Application services
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register user with email - orchestrates validation and creation
     */
    @Transactional
    public AuthResponse registerWithEmail(EmailRegisterRequest request) {
        // Application logic: validate input
        authValidationService.validateEmailFormat(request.getEmail());
        authValidationService.validatePasswordStrength(request.getPassword());

        if (authQueryService.existsByEmail(request.getEmail())) {
            return handleExistingEmailRegistration(request.getEmail());
        }

        // Orchestrate domain operations
        UserPort savedUser = authCommandService.createUser(request.getEmail(), request.getFullName());
        authCommandService.createLocalAuthProvider(savedUser.getUserId(), request.getEmail(), request.getPassword());

        // Application logic: generate token and build response
        return buildAuthResponse(savedUser);
    }

    /**
     * Login with email - orchestrates validation and authentication
     */
    public AuthResponse loginWithEmail(EmailLoginRequest request) {
        // Application logic: validate input
        authValidationService.validateEmailFormat(request.getEmail());

        Optional<AuthProvider> authProviderOpt = authQueryService.findAuthProvider(
                AuthProvider.Provider.LOCAL, request.getEmail());

        if (authProviderOpt.isEmpty()) {
            return handleMissingLocalProvider(request.getEmail());
        }

        AuthProvider authProvider = authProviderOpt.get();
        authValidationService.validateAuthProvider(authProvider);

        // Use domain service for password verification
        if (!authValidationService.verifyAuthProviderPassword(authProvider, request.getPassword(), passwordEncoder)) {
            throw AuthenticationException.invalidCredentials();
        }

        UserPort user = authQueryService.getUserById(authProvider.getUserId());
        authValidationService.validateUserStatus(user);

        return buildAuthResponse(user);
    }

    /**
     * Set password for existing user - orchestrates validation and update
     */
    @Transactional
    public AuthResponse setPasswordForExistingUser(String email, SetPasswordWithEmailRequest request) {
        // Application logic: validate input
        authValidationService.validateEmailFormat(email);
        authValidationService.validatePasswordStrength(request.getPassword());
        authValidationService.validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        UserPort user = authQueryService.findUserByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found", "USER_NOT_FOUND"));

        if (authQueryService.hasLocalAuthProvider(user.getUserId())) {
            throw new ValidationException("User already has password set", "PASSWORD_ALREADY_SET");
        }

        // Orchestrate domain operations
        authCommandService.createLocalAuthProvider(user.getUserId(), email, request.getPassword());

        return buildAuthResponse(user);
    }

    /**
     * Login with Google - orchestrates validation and authentication
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleInfo = googleAuthService.verifyGoogleToken(request.getIdToken());
        authValidationService.validateEmailFormat(googleInfo.getEmail());

        Optional<AuthProvider> authProviderOpt = authQueryService.findAuthProvider(
                AuthProvider.Provider.GOOGLE, googleInfo.getEmail());

        if (authProviderOpt.isPresent()) {
            return handleExistingGoogleUser(authProviderOpt.get());
        } else {
            return registerWithGoogle(googleInfo);
        }
    }

    /**
     * Register with Google - orchestrates user creation and provider setup
     */
    @Transactional
    private AuthResponse registerWithGoogle(GoogleUserInfo googleInfo) {
        UserPort user;

        Optional<UserPort> existingUserOpt = authQueryService.findUserByEmail(googleInfo.getEmail());

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            user = authCommandService.updateUserFromGoogle(user, googleInfo);
        } else {
            user = authCommandService.createUser(googleInfo.getEmail(), googleInfo.getName());
        }

        authCommandService.createGoogleAuthProvider(user.getUserId(), googleInfo.getEmail());

        return buildAuthResponse(user);
    }

    /**
     * Send phone OTP - delegates to OTP service
     */
    public void sendPhoneOtp(PhoneLoginRequest request) {
        authValidationService.validatePhoneFormat(request.getPhone());
        otpService.generateAndSendOtp(request.getPhone());
    }

    /**
     * Login with phone - orchestrates OTP verification and authentication
     */
    @Transactional
    public AuthResponse loginWithPhone(OtpVerifyRequest request) {
        authValidationService.validatePhoneFormat(request.getPhone());

        if (!otpService.verifyOtp(request.getPhone(), request.getCode())) {
            throw new ValidationException("Invalid or expired OTP", "INVALID_OTP");
        }

        Optional<AuthProvider> authProviderOpt = authQueryService.findAuthProvider(
                AuthProvider.Provider.PHONE, request.getPhone());

        if (authProviderOpt.isPresent()) {
            return handleExistingPhoneUser(authProviderOpt.get(), request.getPhone());
        } else {
            return registerWithPhone(request.getPhone());
        }
    }

    /**
     * Register with phone - orchestrates user creation and provider setup
     */
    @Transactional
    private AuthResponse registerWithPhone(String phone) {
        UserPort savedUser = authCommandService.createUserWithoutEmail();
        authCommandService.createPhoneAuthProvider(savedUser.getUserId(), phone);

        return buildAuthResponse(savedUser);
    }

    // ===== PRIVATE HELPER METHODS (Application Logic) =====
    /**
     * Handle existing email registration scenario
     */
    private AuthResponse handleExistingEmailRegistration(String email) {
        UserPort existingUser = authQueryService.findUserByEmail(email).get();
        Optional<AuthProvider> localAuth = authQueryService.findAuthProviderByUserAndProvider(
                existingUser.getUserId(), AuthProvider.Provider.LOCAL);

        if (localAuth.isPresent()) {
            throw new DuplicateResourceException("User", "email", email);
        } else {
            throw new ValidationException(
                    "Tài khoản này đã được tạo bằng Google/Phone. Bạn muốn thiết lập mật khẩu để đăng nhập bằng email không?",
                    "ACCOUNT_EXISTS_OTHER_PROVIDER");
        }
    }

    /**
     * Handle missing local provider scenario
     */
    private AuthResponse handleMissingLocalProvider(String email) {
        if (authQueryService.existsByEmail(email)) {
            throw new ValidationException(
                    "Tài khoản này đã được tạo bằng Google/Phone. Bạn muốn thiết lập mật khẩu để đăng nhập bằng email không?",
                    "ACCOUNT_EXISTS_OTHER_PROVIDER");
        }
        throw AuthenticationException.invalidCredentials();
    }

    /**
     * Handle existing Google user authentication
     */
    private AuthResponse handleExistingGoogleUser(AuthProvider authProvider) {
        UserPort user = authQueryService.getUserById(authProvider.getUserId());
        authValidationService.validateUserStatus(user);

        return buildAuthResponse(user);
    }

    /**
     * Handle existing phone user authentication
     */
    private AuthResponse handleExistingPhoneUser(AuthProvider authProvider, String phone) {
        UserPort user = authQueryService.getUserById(authProvider.getUserId());
        authValidationService.validateUserStatus(user);

        return buildAuthResponse(user);
    }

    /**
     * Build AuthResponse with JWT token - application logic
     */
    private AuthResponse buildAuthResponse(UserPort user) {
        String identifier = user.getEmail();
        if (identifier == null) {
            identifier = "user_" + user.getUserId();
        }

        String token = jwtService.generateToken(
                identifier,
                user.getRole().name(),
                user.getUserId());

        return authMapper.mapToAuthResponseWithToken(user, token);
    }
}
