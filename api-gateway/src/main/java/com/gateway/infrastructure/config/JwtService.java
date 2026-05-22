package com.gateway.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service để xử lý JWT Token
 * Cung cấp các phương thức để giải mã và validate JWT token
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Tạo SecretKey từ chuỗi secret key
     *
     * @return SecretKey dùng để ký và verify JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Giải mã JWT Token và lấy tất cả claims
     *
     * @param token JWT Token cần giải mã
     * @return Claims object chứa tất cả thông tin trong token
     * @throws JwtException nếu token không hợp lệ hoặc đã hết hạn
     */
    public Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Lấy userId từ JWT Token
     *
     * @param token JWT Token
     * @return userId (String)
     * @throws JwtException nếu token không hợp lệ hoặc không có claim userId
     */
    public String extractUserId(String token) throws JwtException {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj == null) {
            throw new JwtException("UserId claim not found in token");
        }

        return userIdObj.toString();
    }

    /**
     * Lấy username (sub) từ JWT Token
     *
     * @param token JWT Token
     * @return username từ claim sub
     * @throws JwtException nếu token không hợp lệ hoặc không có claim sub
     */
    public String extractUsername(String token) throws JwtException {
        Claims claims = extractAllClaims(token);
        String subject = claims.getSubject();

        if (subject == null) {
            throw new JwtException("Subject claim not found in token");
        }

        return subject;
    }

    /**
     * Lấy fullname từ JWT Token
     *
     * @param token JWT Token
     * @return fullname từ claim fullname, null nếu không có
     * @throws JwtException nếu token không hợp lệ
     */
    public String extractFullname(String token) throws JwtException {
        Claims claims = extractAllClaims(token);
        Object fullnameObj = claims.get("fullname");

        if (fullnameObj == null) {
            return null;
        }

        return fullnameObj.toString();
    }

    /**
     * Lấy expiration date từ JWT Token
     *
     * @param token JWT Token
     * @return Date expiration
     * @throws JwtException nếu token không hợp lệ
     */
    public Date extractExpiration(String token) throws JwtException {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Kiểm tra token đã hết hạn chưa
     *
     * @param token JWT Token
     * @return true nếu token đã hết hạn, false nếu còn hiệu lực
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            log.error("Lỗi khi kiểm tra expiration của token: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validate JWT Token
     *
     * @param token JWT Token cần validate
     * @return true nếu token hợp lệ và chưa hết hạn
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.error("JWT Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy secret key (để test)
     *
     * @return secret key string
     */
    public String getJwtSecret() {
        return jwtSecret;
    }

    /**
     * Lấy expiration time (để test)
     *
     * @return expiration time in milliseconds
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
