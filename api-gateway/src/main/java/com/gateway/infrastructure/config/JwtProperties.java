package com.gateway.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Properties - Bind configuration từ application.yml
 *
 * Class này map các configuration có prefix "jwt" trong application.yml
 *
 * Example trong application.yml:
 * <pre>
 * jwt:
 *   secret: 5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
 *   expiration: 86400000
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key dùng để ký và verify JWT tokens
     * Nên sử dụng environment variable trong production thay vì hardcode
     */
    private String secret;

    /**
     * Thời gian hết hạn của JWT token (milliseconds)
     * Default: 86400000ms = 24 giờ
     */
    private Long expiration = 86400000L;

    /**
     * Issuer của JWT token
     */
    private String issuer = "api-gateway";

    /**
     * Authentication type sẽ được set trong Authorization header
     */
    private String authType = "Bearer";
}
