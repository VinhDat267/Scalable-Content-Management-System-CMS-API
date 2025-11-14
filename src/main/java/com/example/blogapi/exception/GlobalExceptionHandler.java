package com.example.blogapi.exception;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.blogapi.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the application
 * 
 * 🎯 XỬ LÝ:
 * - Validation errors (@Valid)
 * - Resource not found (404)
 * - Access denied (403) ← NEW!
 * - Authentication failed (401) ← NEW!
 * - Generic exceptions (500)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // ========== 1. VALIDATION ERRORS (400 Bad Request) ==========

        /**
         * Handle validation errors from @Valid annotations
         * Example: @NotBlank, @Size, @Pattern violations
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex,
                        WebRequest request) {

                log.warn("Validation failed for request: {}", request.getDescription(false));

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                        log.debug("Validation error -Field: {}, Message: {}", fieldName, errorMessage);
                });

                ApiResponse<Object> response = ApiResponse.<Object>builder()
                                .success(false)
                                .message("Validation failed")
                                .data(errors)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ========== 2. RESOURCE NOT FOUND (404 Not Found) ==========

        /**
         * Handle ResourceNotFoundException
         * Example: Post, User, Comment not found by ID
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {

                log.warn("Resource not found: {}", ex.getMessage());

                ApiResponse<Object> response = ApiResponse.<Object>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // ========== 3. ACCESS DENIED (403 Forbidden) ==========

        /**
         * Handle Spring Security AccessDeniedException
         * 
         * 🔒 XẢY RA KHI:
         * - User đã login nhưng không có quyền thực hiện action
         * - @PreAuthorize check fails (user không phải author)
         * 
         * 📝 VÍ DỤ:
         * - Bob tries to update Alice's post → 403
         * - Regular user tries to access admin endpoint → 403
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex,
                        WebRequest request) {

                log.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getDescription(false));

                ApiResponse<Object> response = ApiResponse.<Object>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.FORBIDDEN.value())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        }

        // ========== 4. AUTHENTICATION FAILED (401 Unauthorized) ← NEW! ==========

        /**
         * Handle Spring Security AuthenticationException
         * 
         * 🔐 XẢY RA KHI:
         * - User chưa login (no token in header)
         * - JWT token invalid (expired, malformed, wrong signature)
         * - Login failed (wrong username/password)
         * 
         * 📝 VÍ DỤ:
         * - POST /api/v1/posts without Authorization header → 401
         * - Request with expired JWT token → 401
         * - POST /api/v1/auth/login with wrong password → 401
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex,
                        WebRequest request) {

                log.warn("Authentication failed: {} - Path: {}",
                                ex.getMessage(),
                                request.getDescription(false));

                ApiResponse<Object> response = ApiResponse.<Object>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // ========== 5. ILLEGAL ARGUMENT (400 Bad Request) ==========

        /**
         * Handle IllegalArgumentException
         * Example: Username already exists, invalid parameters
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        WebRequest request) {

                log.warn("Illegal argument: {}", ex.getMessage());

                ApiResponse<Object> response = ApiResponse.<Object>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ========== 6. GENERIC EXCEPTION (500 Internal Server Error) ==========

        /**
         * Handle all other unhandled exceptions
         * Catch-all for unexpected errors
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGlobalException(
                        Exception ex,
                        WebRequest request) {

                log.error("Unhandled exception: {} - Path: {}",
                                ex.getMessage(),
                                request.getDescription(false),
                                ex);

                ApiResponse<Object> response = ApiResponse.<Object>builder()
                                .success(false)
                                .message("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.")
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

}
