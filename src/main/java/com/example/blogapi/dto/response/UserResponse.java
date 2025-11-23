package com.example.blogapi.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
