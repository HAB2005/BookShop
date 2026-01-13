package com.example.system_backend.auth.dto;

import lombok.Data;

@Data
public class AuthResponse {

    public Integer userId;
    public String username;
    public String fullName;
    public String role;
    public String token;
    public String tokenType = "Bearer";
}
