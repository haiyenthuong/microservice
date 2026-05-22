package com.gateway.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler cho API Gateway
 *
 * Xử lý tất cả các exception xảy ra trong Gateway và trả về response format nhất quán
 *
 * Order: -2 để đảm bảo handler này được gọi trước các default handler khác
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Xác định status code và message dựa trên exception type
        HttpStatus status;
        String message;
        String errorType;

        if (ex instanceof ResponseStatusException rse) {
            status = (HttpStatus) rse.getStatusCode();
            message = rse.getReason() != null ? rse.getReason() : ex.getMessage();
            errorType = "ResponseStatusException";
        } else if (ex instanceof JwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid or expired JWT token: " + ex.getMessage();
            errorType = "JwtException";
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
            errorType = "IllegalArgumentException";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Internal server error";
            errorType = "InternalServerError";
        }

        // Set response status và content type
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Log error
        logError(exchange, ex, status, errorType);

        // Tạo error response body
        Map<String, Object> errorBody = createErrorResponse(
                status,
                message,
                errorType,
                exchange.getRequest().getPath().value()
        );

        // Convert thành JSON và write to response
        try {
            String jsonBody = objectMapper.writeValueAsString(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(
                    jsonBody.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error converting exception to JSON", e);
            return Mono.error(ex);
        }
    }

    /**
     * Tạo error response body với format nhất quán
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String message,
                                                     String errorType, String path) {
        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("type", errorType);
        errorResponse.put("path", path);

        return errorResponse;
    }

    /**
     * Log error với đầy đủ thông tin
     */
    private void logError(ServerWebExchange exchange, Throwable ex,
                          HttpStatus status, String errorType) {
        log.error("=== GATEWAY ERROR ===");
        log.error("Error Type: {}", errorType);
        log.error("HTTP Status: {} {}", status.value(), status.getReasonPhrase());
        log.error("Path: {}", exchange.getRequest().getPath().value());
        log.error("Method: {}", exchange.getRequest().getMethod());
        log.error("Message: {}", ex.getMessage());
        log.error("=====================", ex);
    }
}
