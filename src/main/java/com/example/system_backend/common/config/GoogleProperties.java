package com.example.system_backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {
    
    /**
     * Google OAuth2 Client ID for token verification
     */
    private String clientId = "your-google-client-id";
}