package com.example.blogapi.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.blogapi.config.JwtProperties;
import com.example.blogapi.dto.request.AuthRequest;
import com.example.blogapi.dto.response.AuthResponse;
import com.example.blogapi.entity.User;
import com.example.blogapi.repository.UserRepository;
import com.example.blogapi.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider jwtTokenProvider;
        private final UserRepository userRepository;
        private final JwtProperties jwtProperties;

        /**
         * Login và generate JWT token
         * 
         * 📚 OPTIMIZED VERSION:
         * - Extract UserDetails từ Authentication object
         * - Không cần query DB thêm lần nữa
         * - Better performance: 1 DB query thay vì 2
         * 
         * @param request AuthRequest chứa username/password
         * @return AuthResponse chứa JWT token và user info
         */
        public AuthResponse login(AuthRequest request) {
                log.info("Login attempt for username: {}", request.getUsername());

                // ========== STEP 1: Authenticate username/password ==========
                // authenticationManager sẽ:
                // 1. Load user từ DB (via CustomUserDetailsService)
                // 2. Verify password với BCrypt
                // 3. Check account status (enabled, locked, expired)
                // 4. Return Authentication object chứa UserDetails
                Authentication authentication = authenticationManager
                                .authenticate(new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));

                log.info("User authenticated successfully: {}", request.getUsername());

                // ========== STEP 2: Extract UserDetails từ Authentication ==========
                // ✅ Sử dụng UserDetails đã được load trong authentication
                // ❌ Thay vì: userDetailsService.loadUserByUsername() (duplicate query)
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                log.debug("UserDetails extracted from Authentication: {}", userDetails.getUsername());

                // ========== STEP 3: Generate JWT Token ==========
                String token = jwtTokenProvider.generateToken(userDetails);
                log.info("JWT token generated for user: {}", request.getUsername());

                // ========== STEP 4: Extract role từ authorities ==========
                // UserDetails.getAuthorities() returns Collection<GrantedAuthority>
                // Lấy authority đầu tiên (vì chỉ có 1 role)
                String role = userDetails.getAuthorities().stream()
                                .findFirst()
                                .map(auth -> auth.getAuthority())
                                .orElse("ROLE_USER");

                log.debug("Role extracted from authorities: {}", role);

                // ========== STEP 5: Build Response ==========
                // ✅ Tất cả data lấy từ UserDetails/Authentication
                // ✅ Không cần query DB thêm lần nữa
                return AuthResponse.builder()
                                .token(token)
                                .type("Bearer")
                                .username(userDetails.getUsername())
                                .role(role)
                                .expiresIn(jwtProperties.getExpiration() / 1000)
                                .build();
        }
}
