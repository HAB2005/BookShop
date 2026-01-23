package com.example.system_backend.common.config;

import com.example.system_backend.common.security.CustomAccessDeniedHandler;
import com.example.system_backend.common.security.CustomAuthenticationEntryPoint;
import com.example.system_backend.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final CustomAccessDeniedHandler accessDeniedHandler;
        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Authentication endpoints - public
                                                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                                                .requestMatchers("/api/auth/email/register", "/api/auth/email/login")
                                                .permitAll()
                                                .requestMatchers("/api/auth/google/login").permitAll()
                                                .requestMatchers("/api/auth/phone/send-otp",
                                                                "/api/auth/phone/verify-otp")
                                                .permitAll()
                                                // Product endpoints - public (for home/shop page)
                                                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*",
                                                                "/api/products/search",
                                                                "/api/products/suggestions")
                                                .permitAll()
                                                // Product images - public read, admin write
                                                .requestMatchers(HttpMethod.GET, "/api/product-images",
                                                                "/api/product-images/*",
                                                                "/api/product-images/primary",
                                                                "/api/product-images/stats")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/product-images")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/product-images/*",
                                                                "/api/product-images/reorder")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/product-images/*/primary")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/product-images",
                                                                "/api/product-images/*")
                                                .hasRole("ADMIN")
                                                // File serving - public
                                                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()
                                                // Product management - admin only
                                                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/products/*/status")
                                                .hasRole("ADMIN")
                                                // Logout - authenticated
                                                .requestMatchers("/api/auth/logout").authenticated()
                                                // Role-based access
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                                                // User endpoints - authenticated
                                                .requestMatchers("/api/user/**").authenticated()
                                                // Cart endpoints - authenticated
                                                .requestMatchers("/api/cart/**").authenticated()
                                                // Order endpoints - authenticated
                                                .requestMatchers("/api/orders/**").authenticated()
                                                // All other requests - authenticated
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedHandler(accessDeniedHandler)
                                                .authenticationEntryPoint(authenticationEntryPoint))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
