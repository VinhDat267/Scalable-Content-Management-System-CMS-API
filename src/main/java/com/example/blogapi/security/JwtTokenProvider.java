package com.example.blogapi.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.blogapi.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Token Provider - Generate, validate và extract JWT tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Generate JWT token từ UserDetails
     * 
     * @param userDetails Spring Security UserDetails
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate JWT token với custom claims
     * 
     * @param extraClaims Additional claims (userId, role, etc.)
     * @param userDetails Spring Security UserDetails
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Create JWT token với claims và subject
     * 
     * 🎯 CƠ CHẾ:
     * 1. Set claims (payload data)
     * 2. Set subject (username)
     * 3. Set issued time (thời điểm tạo)
     * 4. Set expiration time (thời điểm hết hạn)
     * 5. Sign với secret key
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        log.debug("Creating JWT token for user: {} with expiration: {}", subject, expiryDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     * 
     * @param token       JWT token string
     * @param userDetails UserDetails to validate against
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));

            if (isValid) {
                log.debug("JWT token validated successfully for user: {}", username);
            } else {
                log.warn("JWT token validation failed for user: {}", username);
            }

            return isValid;
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username từ JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date từ JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim từ JWT token
     * 
     * @param token          JWT token
     * @param claimsResolver Function để extract claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims từ JWT token
     * 
     * 🔑 KEY POINT:
     * - Parse token với signing key
     * - Verify signature
     * - Return payload (claims)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Get signing key từ secret
     * 
     * 🔐 SECURITY:
     * - Decode base64 secret
     * - Create HMAC SHA key
     * - Key này dùng để sign & verify JWT
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}