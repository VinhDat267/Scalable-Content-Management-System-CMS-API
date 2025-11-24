package com.example.blogapi.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    Long id;
    String title;
    String content;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;
    LocalDateTime deletedAt;
    String deletedBy;
    String authorUsername;

}
