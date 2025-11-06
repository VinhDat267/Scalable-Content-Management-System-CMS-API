package com.example.blogapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Component
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
@Validated
public class JwtProperties {
    @NotBlank(message = "JWT secret key không được để trống")
    private String secret;

    @Positive(message = "JWT expiration time phải là số dương")
    private long expiration;
}
