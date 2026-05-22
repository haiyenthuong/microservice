package com.gateway.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Cấu hình CORS (Cross-Origin Resource Sharing) cho API Gateway
 *
 * Cho phép requests từ các origin khác nhau truy cập vào API
 *
 * Lưu ý: Mặc định Spring Cloud Gateway đã có CORS configuration trong application.yml
 * Class này cung cấp thêm flexibility nếu cần custom complex CORS rules
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * Cấu hình CORS Filter cho phép các origin được phép truy cập
     *
     * Cấu hình này:
     * - Cho phép tất cả origins (*)
     * - Cho phép tất cả HTTP methods (GET, POST, PUT, DELETE, etc.)
     * - Cho phép tất cả headers
     * - Cho phép credentials (cookies, authorization headers)
     * - Max age: 1 giờ (3600 seconds)
     *
     * @return CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Cho phép tất cả origins
        // Trong production, nên limit cụ thể: corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://example.com"))
        corsConfig.addAllowedOriginPattern("*");

        // Cho phép credentials (quan trọng khi gửi JWT token qua cookies)
        corsConfig.setAllowCredentials(true);

        // Cho phép tất cả headers
        corsConfig.addAllowedHeader("*");

        // Cho phép tất cả methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH",
                "HEAD"
        ));

        // Exposed headers (headers mà client được phép đọc từ response)
        corsConfig.setExposedHeaders(Arrays.asList(
                "X-User-Id",
                "X-User-Name",
                "X-User-Fullname",
                "Authorization",
                "Content-Type",
                "Content-Disposition"
        ));

        // Max age: browser sẽ cache preflight response trong 1 giờ
        corsConfig.setMaxAge(3600L);

        // Apply CORS configuration cho tất cả paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        log.info("CORS Configuration initialized - Allowed Origins: *, Allowed Methods: {}, Max Age: {}s",
                corsConfig.getAllowedMethods(), corsConfig.getMaxAge());

        return new CorsWebFilter(source);
    }
}
