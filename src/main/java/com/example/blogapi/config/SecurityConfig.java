package com.example.blogapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.blogapi.security.CustomAccessDeniedHandler;
import com.example.blogapi.security.CustomAuthenticationEntryPoint;
import com.example.blogapi.security.JwtAuthenticationFilter;
import com.example.blogapi.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomUserDetailsService userDetailsService;

        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers
                                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                                .authorizeHttpRequests(authz -> authz
                                                // ========== PUBLIC ENDPOINTS ==========

                                                // Authentication & Registration
                                                .requestMatchers(
                                                                "/api/v1/auth/**",
                                                                "/api/v1/users/register")
                                                .permitAll()

                                                // Swagger UI (Complete list)
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**", // Swagger UI
                                                                "/v3/api-docs/**", // OpenAPI docs
                                                                "/swagger-resources/**",
                                                                "/configuration/**",
                                                                "/webjars/**")
                                                .permitAll()

                                                // H2 Console (Development only)
                                                .requestMatchers("/h2-console/**").permitAll()

                                                // ========== READ-ONLY ENDPOINTS (Public) ==========
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/posts/**",
                                                                "/api/v1/users/**",
                                                                "/api/v1/comments/**")
                                                .permitAll()

                                                // ❌ Tất cả requests khác cần authentication
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}