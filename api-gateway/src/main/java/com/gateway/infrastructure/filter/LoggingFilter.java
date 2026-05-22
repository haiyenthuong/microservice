package com.gateway.infrastructure.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Logging Filter cho API Gateway
 *
 * Filter này được áp dụng cho TẤT CẢ requests đi qua gateway
 * - Log request details khi request đến
 * - Log response details khi response trả về
 *
 * Order: HIGHEST_PRECEDENCE (-1) để đảm bảo log được đầu tiên
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Lưu start time để tính duration
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        // Log request incoming
        logRequest(request);

        // Xử lý request và log response khi complete
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute(START_TIME_ATTR);
            Long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            logResponse(request, response, duration);
        }));
    }

    /**
     * Log thông tin request
     */
    private void logRequest(ServerHttpRequest request) {
        log.info("=== INCOMING REQUEST ===");
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", request.getURI().getPath());
        log.info("Query: {}", request.getURI().getQuery());
        log.info("Remote Address: {}", request.getRemoteAddress());
        log.info("Headers:");
        request.getHeaders().forEach((key, value) -> {
            if (shouldLogHeader(key)) {
                log.info("  {}: {}", key, value);
            }
        });
        log.info("========================");
    }

    /**
     * Log thông tin response
     */
    private void logResponse(ServerHttpRequest request, ServerHttpResponse response, long duration) {
        log.info("=== OUTGOING RESPONSE ===");
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", request.getURI().getPath());
        log.info("Status: {}", response.getStatusCode());
        log.info("Duration: {} ms", duration);
        log.info("Headers:");
        response.getHeaders().forEach((key, value) -> {
            if (shouldLogHeader(key)) {
                log.info("  {}: {}", key, value);
            }
        });
        log.info("=========================");
    }

    /**
     * Quyết định xem có nên log header này không
     * Không log sensitive headers như Authorization, Cookie
     */
    private boolean shouldLogHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return !lower.equals("authorization") &&
                !lower.equals("cookie") &&
                !lower.equals("set-cookie");
    }

    /**
     * Order để filter này chạy đầu tiên
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
