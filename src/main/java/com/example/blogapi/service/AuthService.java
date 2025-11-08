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
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        log.info("User authenticated successfully: {}", request.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String token = jwtTokenProvider.generateToken(userDetails);
        log.info("JWT token generated for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .build();
    }
}
