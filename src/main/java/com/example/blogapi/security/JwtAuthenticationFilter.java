package com.example.blogapi.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter
 * 
 * 🎯 NHIỆM VỤ:
 * 1. Intercept mọi HTTP request
 * 2. Extract JWT token từ Authorization header
 * 3. Validate token
 * 4. Set authentication vào SecurityContext
 * 
 * 📚 FLOW:
 * Request → Filter → Extract Token → Validate → Set Auth → Controller
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Filter logic chạy cho MỌI request
     * 
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain Filter chain để pass request tiếp
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // ========== STEP 1: Extract JWT token từ header ==========
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Check Authorization header có đúng format không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request header");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (bỏ "Bearer " prefix)
        jwt = authHeader.substring(7);
        log.debug("JWT token extracted from header");

        // ========== STEP 2: Extract username từ token ==========
        try {
            username = jwtTokenProvider.extractUsername(jwt);
            log.debug("Username extracted from JWT: {}", username);
        } catch (Exception e) {
            log.error("Failed to extract username from JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // ========== STEP 3: Validate token và set authentication ==========
        // Check user chưa được authenticate (tránh duplicate)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details từ database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate token
            if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                log.info("JWT token validated successfully for user: {}", username);

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                // Set details (IP, session, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authentication set in SecurityContext for user: {}", username);
            } else {
                log.warn("JWT token validation failed for user: {}", username);
            }
        }

        // ========== STEP 4: Pass request to next filter ==========
        filterChain.doFilter(request, response);
    }
}