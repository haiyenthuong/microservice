package com.auth.infrastructure.security;

import com.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Service để generate và validate JWT tokens
 *
 * Chức năng:
 * - Generate access token và refresh token
 * - Validate token
 * - Extract claims từ token
 * - Refresh token management
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:900000}")       // 15 minutes
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")   // 7 days
    private long refreshTokenExpiration;

    /**
     * Secret key để sign tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token cho user
     *
     * @param user User entity
     * @return JWT access token string
     */
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    /**
     * Generate refresh token cho user
     *
     * @param user User entity
     * @return JWT refresh token string
     */
    public String generateRefreshToken(User user) {
        String refreshToken = generateToken(user, refreshTokenExpiration);

        // Lưu refresh token vào cache (Redis hoặc in-memory)
        // Trong production, nên dùng Redis
        refreshTokensCache.put(user.getId(), refreshToken);

        return refreshToken;
    }

    /**
     * Generate JWT token cho user với expiration time cụ thể
     *
     * @param user        User entity
     * @param expiration Token expiration time in milliseconds
     * @return JWT token string
     */
    private String generateToken(User user, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // Extract roles, permissions từ user (nếu có)
        List<String> roles = extractUserRoles(user);
        List<String> permissions = extractUserPermissions(user);
        List<String> groups = extractUserGroups(user);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("fullname", user.getFullname())
                .claim("type", user.getUserType().getValue())
                .claim("status", user.getUserStatus().getValue())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("groups", groups)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token cần validate
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract tất cả claims từ token
     *
     * @param token JWT token
     * @return Claims object
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user ID từ token
     *
     * @param token JWT token
     * @return User ID
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Extract username từ token
     *
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract fullname từ token
     *
     * @param token JWT token
     * @return Full name
     */
    public String extractFullname(String token) {
        return extractAllClaims(token).get("fullname", String.class);
    }

    /**
     * Extract email từ token
     *
     * @param token JWT token
     * @return Email
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extract user type từ token
     *
     * @param token JWT token
     * @return User type value
     */
    public Integer extractUserType(String token) {
        return extractAllClaims(token).get("type", Integer.class);
    }

    /**
     * Extract user status từ token
     *
     * @param token JWT token
     * @return User status value
     */
    public Integer extractUserStatus(String token) {
        return extractAllClaims(token).get("status", Integer.class);
    }

    /**
     * Extract roles từ token
     *
     * @param token JWT token
     * @return List of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    /**
     * Extract permissions từ token
     *
     * @param token JWT token
     * @return List of permissions
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractAllClaims(token).get("permissions", List.class);
    }

    /**
     * Extract groups từ token
     *
     * @param token JWT token
     * @return List of groups
     */
    @SuppressWarnings("unchecked")
    public List<String> extractGroups(String token) {
        return extractAllClaims(token).get("groups", List.class);
    }

    /**
     * Get access token expiration time
     *
     * @return Expiration time in milliseconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Get refresh token expiration time
     *
     * @return Expiration time in milliseconds
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Validate refresh token và extract user ID
     *
     * @param refreshToken Refresh token
     * @return User ID nếu valid, null nếu không
     */
    public String validateRefreshToken(String refreshToken) {
        try {
            if (!validateToken(refreshToken)) {
                return null;
            }

            String userId = extractUserId(refreshToken);

            // Kiểm tra refresh token có trong cache không
            String cachedToken = refreshTokensCache.get(userId);
            if (cachedToken != null && cachedToken.equals(refreshToken)) {
                return userId;
            }

            return null;
        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Revoke refresh token (logout)
     *
     * @param userId User ID
     */
    public void revokeRefreshToken(String userId) {
        refreshTokensCache.remove(userId);
        log.info("Refresh token revoked for user: {}", userId);
    }

    /**
     * Revoke tất cả refresh tokens của user
     *
     * @param userId User ID
     */
    public void revokeAllUserTokens(String userId) {
        refreshTokensCache.remove(userId);
        log.info("All tokens revoked for user: {}", userId);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Extract user roles từ user entity
     * TODO: Implement khi có Group và Authority logic
     *
     * @param user User entity
     * @return List of role names
     */
    private List<String> extractUserRoles(User user) {
        List<String> roles = new ArrayList<>();

        // Default role based on user type
        if (user.isAdmin()) {
            roles.add("ROLE_ADMIN");
        } else if (user.isCustomer()) {
            roles.add("ROLE_CUSTOMER");
        } else {
            roles.add("ROLE_USER");
        }

        return roles;
    }

    /**
     * Extract user permissions từ user entity
     * TODO: Implement khi có Group và Authority logic
     *
     * @param user User entity
     * @return List of permissions
     */
    private List<String> extractUserPermissions(User user) {
        List<String> permissions = new ArrayList<>();

        // Default permissions based on user type
        if (user.isAdmin()) {
            permissions.add("USER_READ");
            permissions.add("USER_WRITE");
            permissions.add("USER_DELETE");
            permissions.add("ROLE_READ");
            permissions.add("ROLE_WRITE");
            permissions.add("ROLE_DELETE");
            permissions.add("GROUP_READ");
            permissions.add("GROUP_WRITE");
            permissions.add("PERMISSION_READ");
            permissions.add("PERMISSION_WRITE");
            permissions.add("PARAMETER_READ");
            permissions.add("PARAMETER_WRITE");
        } else if (user.isCustomer()) {
            permissions.add("USER_READ");
            permissions.add("USER_WRITE_OWN");
        }

        return permissions;
    }

    /**
     * Extract user groups từ user entity
     * TODO: Implement khi có Group logic
     *
     * @param user User entity
     * @return List of group names
     */
    private List<String> extractUserGroups(User user) {
        List<String> groups = new ArrayList<>();

        // TODO: Query from GroupUser entity
        // For now, return default group based on user type
        if (user.isAdmin()) {
            groups.add("ADMINS");
        } else if (user.isCustomer()) {
            groups.add("CUSTOMERS");
        }

        return groups;
    }

    // ==================== Refresh Token Cache (In-Memory) ====================

    /**
     * Cache cho refresh tokens
     * Trong production, nên dùng Redis
     */
    private final Map<String, String> refreshTokensCache = new ConcurrentHashMap<>();
}
