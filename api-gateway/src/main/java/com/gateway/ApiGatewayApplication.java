package com.gateway;

import com.gateway.infrastructure.filter.AuthorizeFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Main Application class cho API Gateway
 *
 * API Gateway đóng vai trò là：
 * 1. Entry point duy nhất cho tất cả client requests
 * 2. Centralized authentication (JWT validation)
 * 3. Routing requests đến các microservices phù hợp
 * 4. Cross-cutting concerns (CORS, logging, etc.)
 *
 * Architecture:
 * - Client → API Gateway (8080) → Microservices (8081, 8082, 8083)
 *
 * Security:
 * - JWT Token validation được thực hiện tại đây
 * - Thông tin user được inject qua headers (X-User-Id, X-User-Name, X-User-Fullname)
 * - Microservices không cần validate JWT lại, chỉ cần đọc headers
 */
@SpringBootApplication
public class ApiGatewayApplication {

    private static final String CMS_SERVICE_URL = "http://localhost:8081";
    private static final String ORDER_SERVICE_URL = "http://localhost:8082";
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8083";

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Cấu hình routes bằng cách lập trình (Programmatic Route Configuration)
     * Đây là alternative cho cấu hình trong application.yml
     *
     * Lưu ý: Nếu sử dụng cả application.yml và Bean này, routes sẽ được merge.
     * Để tránh conflict, nên chọn một trong hai cách.
     *
     * Bean này được comment mặc định, ưu tiên sử dụng application.yml
     * Nếu muốn dùng programmatic configuration, uncomment bean này và comment routes trong application.yml
     */
    //@Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Route cho CMS Service
                .route("cms-service", r -> r
                        .path("/api/cms/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(new AuthorizeFilter().apply(new AuthorizeFilter.Config()))
                        )
                        .uri(CMS_SERVICE_URL)
                )

                // Route cho Order Service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(new AuthorizeFilter().apply(new AuthorizeFilter.Config()))
                        )
                        .uri(ORDER_SERVICE_URL)
                )

                // Route cho Payment Service
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(new AuthorizeFilter().apply(new AuthorizeFilter.Config()))
                        )
                        .uri(PAYMENT_SERVICE_URL)
                )

                // Health check route
                .route("health-check", r -> r
                        .path("/health")
                        .uri("lb://http://localhost:8080/actuator/health")
                )

                .build();
    }
}
