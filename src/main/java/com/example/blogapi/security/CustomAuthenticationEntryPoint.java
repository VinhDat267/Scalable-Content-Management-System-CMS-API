package com.example.blogapi.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.blogapi.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Authentication Entry Point
 * 
 * 🎯 MỤC ĐÍCH:
 * - Handle 401 Unauthorized khi user chưa đăng nhập
 * - Return JSON response thay vì default HTML error page
 * 
 * 🔐 KÍCH HOẠT KHI:
 * - User truy cập protected endpoint mà không có token
 * - JWT token invalid/expired
 * - Authorization header missing
 * 
 * 📝 VÍ DỤ:
 * - POST /api/v1/posts without token → 401
 * - Request with malformed token → 401
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private final ObjectMapper objectMapper;

        @Override
        public void commence(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
                log.warn("Unathorized request to: {} - Reason: {}", request.getRequestURI(),
                                authException.getMessage());

                // Buid error response
                ApiResponse<Object> errorResponse = ApiResponse.<Object>builder()
                                .success(false)
                                .message("Vui lòng đăng nhập để tiếp tục")
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .path(request.getRequestURI())
                                .build();

                // Write JSON response
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                response.getWriter().write(jsonResponse);

        }

}