package com.example.system_backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins - Frontend URLs
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000", // React default
                "http://localhost:3001", // React alternative
                "http://localhost:4200", // Angular default
                "http://localhost:5173", // Vite default
                "http://localhost:8081", // Vue default
                "http://localhost:5500", // Live Server default
                "http://localhost:5501", // Live Server alternative
                "http://127.0.0.1:5500", // Live Server with 127.0.0.1
                "http://127.0.0.1:5501", // Live Server alternative
                "http://127.0.0.1:*", // Local development
                "http://localhost:*" // Any localhost port
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));

        // Exposed headers (Frontend có thể đọc được)
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}