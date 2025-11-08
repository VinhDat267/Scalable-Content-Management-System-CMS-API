package com.example.blogapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO cho login request
 */
@Data
public class AuthRequest {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;
}
