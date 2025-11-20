package com.example.blogapi.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.blogapi.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Access Denied Handler
 * 
 * 🎯 MỤC ĐÍCH:
 * - Handle 403 Forbidden khi user đã login nhưng không có quyền
 * - Return JSON response
 * 
 * 🔐 KÍCH HOẠT KHI:
 * - User đã authenticated nhưng @PreAuthorize check fails
 * - User không phải author của resource
 * - User không có role phù hợp
 * 
 * 📝 VÍ DỤ:
 * - Bob tries to update Alice's post → 403
 * - Regular user tries to access admin endpoint → 403
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied to: {} for user: {} - Reason: {}",
                request.getRequestURI(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                accessDeniedException.getMessage());

        // Build error response
        ApiResponse<Object> errorResponse = ApiResponse.<Object>builder()
                .success(false)
                .message("Bạn không có quyền thực hiện thao tác này")
                .data(null)
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .path(request.getRequestURI())
                .build();

        // Write JSON response
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}