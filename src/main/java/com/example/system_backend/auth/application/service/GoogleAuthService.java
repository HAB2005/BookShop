package com.example.system_backend.auth.application.service;

import com.example.system_backend.auth.dto.GoogleUserInfo;
import com.example.system_backend.auth.exception.GoogleAuthException;
import com.example.system_backend.common.config.GoogleProperties;
import com.example.system_backend.common.exception.AuthenticationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Specialized service for Google OAuth token verification. Handles external
 * provider integration only.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleProperties googleProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleUserInfo verifyGoogleToken(String token) {
        try {
            // Check if Google Client ID is configured
            if (isGoogleClientIdNotConfigured()) {
                log.warn("Google Client ID not configured, using mock verification for development");
                return mockGoogleTokenVerification();
            }

            // Try ID token verification first, then access token
            try {
                return verifyIdToken(token);
            } catch (GoogleAuthException idTokenException) {
                log.debug("ID token verification failed, trying access token: {}", idTokenException.getMessage());
                try {
                    return verifyAccessToken(token);
                } catch (GoogleAuthException accessTokenException) {
                    log.error("Both ID token and access token verification failed", accessTokenException);
                    throw new AuthenticationException("Invalid Google token");
                }
            }

        } catch (AuthenticationException e) {
            // Re-throw authentication exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Google token verification", e);

            // In development, fall back to mock
            if (isGoogleClientIdNotConfigured()) {
                log.warn("Falling back to mock verification due to error in development mode");
                return mockGoogleTokenVerification();
            }

            throw new AuthenticationException("Google token verification failed: " + e.getMessage());
        }
    }

    private GoogleUserInfo verifyIdToken(String idToken) {
        try {
            // Validate token format
            if (idToken == null || idToken.trim().isEmpty()) {
                throw new GoogleAuthException("ID token is null or empty");
            }

            // Check if token looks like a JWT (has 3 parts separated by dots)
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                log.debug("Token doesn't have 3 parts (header.payload.signature), might be access token");
                throw new GoogleAuthException("Invalid ID token format - expected JWT with 3 parts, got " + parts.length);
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleProperties.getClientId()))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();

                return GoogleUserInfo.builder()
                        .googleId(payload.getSubject())
                        .email(payload.getEmail())
                        .name((String) payload.get("name"))
                        .picture((String) payload.get("picture"))
                        .emailVerified(payload.getEmailVerified())
                        .build();
            } else {
                throw new GoogleAuthException("Invalid Google ID token");
            }
        } catch (GoogleAuthException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            // This happens when token format is invalid (e.g., access token instead of ID token)
            log.debug("IllegalArgumentException during ID token verification: {}", e.getMessage());
            throw new GoogleAuthException("Invalid ID token format: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to verify Google ID token", e);
            throw new GoogleAuthException("Failed to verify Google ID token: " + e.getMessage());
        }
    }

    private GoogleUserInfo verifyAccessToken(String accessToken) {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(userInfoUrl))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseGoogleUserInfo(response.body());
            } else {
                throw new GoogleAuthException("Failed to get user info from Google: " + response.statusCode());
            }
        } catch (GoogleAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Google access token", e);
            throw new GoogleAuthException("Failed to verify Google access token: " + e.getMessage());
        }
    }

    private GoogleUserInfo parseGoogleUserInfo(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            String googleId = jsonNode.get("id").asText();
            String email = jsonNode.get("email").asText();
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : null;
            String picture = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;

            if (googleId == null || email == null) {
                throw new GoogleAuthException("Missing required fields from Google response");
            }

            return GoogleUserInfo.builder()
                    .googleId(googleId)
                    .email(email)
                    .name(name != null ? name : email)
                    .picture(picture)
                    .emailVerified(true)
                    .build();
        } catch (GoogleAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Google user info", e);
            throw new GoogleAuthException("Failed to parse Google user info: " + e.getMessage());
        }
    }

    private boolean isGoogleClientIdNotConfigured() {
        String clientId = googleProperties.getClientId();
        return clientId == null
                || clientId.isEmpty()
                || clientId.equals("your-google-client-id");
    }

    private GoogleUserInfo mockGoogleTokenVerification() {
        return GoogleUserInfo.builder()
                .googleId("mock_google_id_123")
                .email("testuser@gmail.com")
                .name("Test User")
                .picture("https://example.com/avatar.jpg")
                .emailVerified(true)
                .build();
    }
}
