package com.example.blogapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho login response
 * 
 * 📦 CHỨA:
 * - JWT token
 * - User info (username, role)
 * - Token type (Bearer)
 * - Expiration time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;

    @Builder.Default
    private String type = "Bearer";

    private String username;
    private String role;
    private long expiresIn;
}
