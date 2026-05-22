package com.gateway.interfaces;

import com.gateway.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller cho API Gateway
 *
 * Controller này chỉ nên được enable trong DEVELOPMENT environment
 * Cung cấp các endpoint để:
 * 1. Generate JWT tokens cho testing
 * 2. Health check
 * 3. Test routing
 *
 * Để enable: set gateway.test.enabled=true trong application.yml
 */
@Slf4j
@RestController
@RequestMapping("/public")
@ConditionalOnProperty(name = "gateway.test.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class TestController {

    private final JwtProperties jwtProperties;

    /**
     * Generate JWT Token cho testing
     *
     * POST /public/generate-token
     * Body:
     * {
     *   "userId": "123e4567-e89b-12d3-a456-426614174000",
     *   "username": "testuser",
     *   "fullname": "Test User"
     * }
     *
     * @param tokenRequest Token request body
     * @return JWT token
     */
    @PostMapping("/generate-token")
    public Mono<Map<String, Object>> generateToken(@RequestBody TokenRequest tokenRequest) {
        log.info("Generating token for user: {}", tokenRequest.getUsername());

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getExpiration());

        String token = Jwts.builder()
                .subject(tokenRequest.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .issuer(jwtProperties.getIssuer())
                .claim("userId", tokenRequest.getUserId())
                .claim("fullname", tokenRequest.getFullname())
                .signWith(key)
                .compact();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("type", jwtProperties.getAuthType());
        response.put("expiresAt", expiration.toString());
        response.put("userId", tokenRequest.getUserId());
        response.put("username", tokenRequest.getUsername());

        return Mono.just(response);
    }

    /**
     * Health check endpoint
     * GET /public/health
     *
     * @return Health status
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "api-gateway");
        health.put("timestamp", Instant.now().toString());
        return Mono.just(health);
    }

    /**
     * Test endpoint để check request headers
     * GET /public/test-headers
     *
     * @param exchange ServerWebExchange
     * @return Headers từ request
     */
    @GetMapping("/test-headers")
    public Mono<Map<String, Object>> testHeaders(ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("path", exchange.getRequest().getPath().value());
        response.put("method", exchange.getRequest().getMethod().name());
        response.put("headers", exchange.getRequest().getHeaders().toSingleValueMap());
        return Mono.just(response);
    }

    /**
     * Token Request DTO
     */
    public static class TokenRequest {
        private String userId;
        private String username;
        private String fullname;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }
    }
}
