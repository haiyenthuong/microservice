package com.cms.infrastructure.config;

import com.cms.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Trích xuất username từ JWT token.
     *
     * @param token JWT token
     * @return username được trích xuất từ token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Trích xuất userId từ JWT token.
     *
     * @param token JWT token
     * @return userId được trích xuất từ token, hoặc null nếu không tồn tại
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Trích xuất claim từ JWT token.
     *
     * @param token JWT token
     * @param claimsResolver function để xử lý claims
     * @return giá trị claim được trích xuất
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Tạo JWT token cho người dùng.
     *
     * @param user thông tin người dùng
     * @return JWT token string
     */
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    /**
     * Tạo JWT token với claims bổ sung.
     *
     * @param extraClaims các claims bổ sung
     * @param user thông tin người dùng
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return buildToken(extraClaims, user, jwtExpiration);
    }

    /**
     * Tạo refresh token cho người dùng.
     *
     * @param user thông tin người dùng
     * @return refresh token string
     */
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("userId", user.getId())
                .claim("fullname", user.getFullname())
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Kiểm tra JWT token có hợp lệ hay không.
     *
     * @param token JWT token cần kiểm tra
     * @param username username để so sánh
     * @return true nếu token hợp lệ
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    /**
     * Kiểm tra JWT token đã hết hạn hay chưa.
     *
     * @param token JWT token cần kiểm tra
     * @return true nếu token đã hết hạn
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
