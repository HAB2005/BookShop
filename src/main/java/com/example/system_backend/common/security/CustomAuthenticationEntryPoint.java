package com.example.system_backend.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.warn("Unauthorized access attempt: {} - {}", request.getMethod(), request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Authentication required to access this resource\",\"path\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now());

        response.getWriter().write(jsonResponse);
    }
}