package com.example.blogapi.dto.response;

import java.time.LocalDateTime;

import com.example.blogapi.exception.ErrorDetails;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private Integer statusCode;
    private String path;

    private PageMetadata pagination;

    private ErrorDetails error;

    // Success respone
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Request processed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(201)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Resource created successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(201)
                .build();
    }

    // Error Response

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorDetails errorDetails, int statusCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .path(path)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }

}
