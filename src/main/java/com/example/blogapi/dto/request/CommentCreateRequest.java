package com.example.blogapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "Comment không được bỏ trống.")
    @Size(min = 1, max = 500, message = "Comment phải có độ dài từ 1 đến 500 ký tự")
    private String body;
}
