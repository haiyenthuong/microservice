package com.gateway.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway Properties - Bind configuration từ application.yml
 *
 * Class này map các configuration có prefix "gateway" trong application.yml
 *
 * Example trong application.yml:
 * <pre>
 * gateway:
 *   excluded-paths:
 *     - /health
 *     - /actuator/**
 *     - /public/**
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

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
