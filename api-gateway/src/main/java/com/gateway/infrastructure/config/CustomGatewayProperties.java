package com.gateway.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Custom Gateway Properties - Bind configuration từ application.yml
 *
 * Class này map các configuration có prefix "custom.gateway" trong
 * application.yml
 * Đổi tên và prefix để tránh xung đột với Spring Cloud Gateway's built-in
 * GatewayProperties
 *
 * Example trong application.yml:
 * 
 * <pre>
 * custom:
 *   gateway:
 *     excluded-paths:
 *       paths:
 *         - /health
 *         - /actuator/**
 *         - /public/**
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "custom.gateway")
public class CustomGatewayProperties {

    /**
     * List các paths không cần authentication
     * Ví dụ: health check, actuator endpoints, public APIs
     */
    private ExcludedPaths excludedPaths = new ExcludedPaths();

    /**
     * Inner class cho excluded paths configuration
     */
    @Data
    public static class ExcludedPaths {
        private boolean enabled = true;
        private String[] paths = {
                "/health",
                "/actuator/**",
                "/public/**"
        };
    }
}
