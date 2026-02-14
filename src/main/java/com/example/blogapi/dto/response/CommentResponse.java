package com.example.blogapi.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    Long id;
    String body;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;
    Long postId;
    String authorUsername;

}
