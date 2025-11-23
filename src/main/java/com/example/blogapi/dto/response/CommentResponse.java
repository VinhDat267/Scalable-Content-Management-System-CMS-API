package com.example.blogapi.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentResponse {
    private Long id;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long postId;
    private String authorUsername;

}
