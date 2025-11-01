package com.example.blogapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/v1/users/register", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // GET endpoints - public
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/posts/**",
                                "/api/v1/posts/search/**",
                                "/api/v1/posts/user/**",
                                "/api/v1/posts/recent/**",
                                "/api/v1/posts/{postId}/comments/**",
                                "/api/v1/users/**",
                                "/api/v1/users/search/**",
                                "/api/v1/users/role/**",
                                "/api/v1/comments/**",
                                "/api/v1/comments/user/**",
                                "/api/v1/comments/search/**")
                        .permitAll()

                        // Tất cả các request khác cần authentication
                        .anyRequest().authenticated())
                .httpBasic(withDefaults());
        return http.build();
    }
}