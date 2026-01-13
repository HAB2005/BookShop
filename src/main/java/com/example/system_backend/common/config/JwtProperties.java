package com.example.system_backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT secret key for signing tokens
     */
    private String secret = "mySecretKey123456789012345678901234567890123456789012345678901234567890";
    
    /**
     * JWT token expiration time in milliseconds (default: 1 hour)
     */
    private Long expiration = 3600000L; // 1 hour
}