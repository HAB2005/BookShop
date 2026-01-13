package com.example.system_backend.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied for request: {} - {}", request.getMethod(), request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"errorCode\":\"ACCESS_DENIED\",\"message\":\"You don't have permission to access this resource\",\"path\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now());

        response.getWriter().write(jsonResponse);
    }
}