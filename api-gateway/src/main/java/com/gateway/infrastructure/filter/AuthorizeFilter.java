package com.gateway.infrastructure.filter;

import com.gateway.infrastructure.config.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Gateway Filter để xác thực JWT Token
 *
 * Filter này:
 * 1. Trích xuất JWT token từ Authorization header
 * 2. Giải mã và validate token bằng JwtService
 * 3. Inject thông tin user vào headers (X-User-Id, X-User-Name,
 * X-User-Fullname)
 * 4. Cho phép request đi qua nếu token hợp lệ
 * 5. Từ chối request nếu token không hợp lệ hoặc không có
 *
 * Cấu hình trong application.yml:
 *
 * <pre>
 * spring.cloud.gateway.routes[0].filters[1] = AuthorizeFilter
 * </pre>
 */
@Slf4j
@Component
public class AuthorizeFilter extends AbstractGatewayFilterFactory<AuthorizeFilter.Config> {

    @Autowired
    private JwtService jwtService;

    /**
     * Constructor mặc định
     * AbstractGatewayFilterFactory yêu cầu constructor không tham số
     */
    public AuthorizeFilter() {
        super(Config.class);
    }

    /**
     * Config class cho filter
     * Có thể mở rộng để thêm các cấu hình tùy chỉnh
     */
    public static class Config {
        // Có thể thêm các thuộc tính cấu hình tại đây nếu cần
        // Ví dụ: boolean required, List<String> excludedPaths, etc.
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Log request incoming
            logRequest(request);

            // Lấy Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Kiểm tra xem có Authorization header không
            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Authorization header is missing for path: {}", request.getURI().getPath());
                return handleError(response, "Authorization header is required");
            }

            // Kiểm tra format của Authorization header (phải bắt đầu bằng "Bearer ")
            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for path: {}", request.getURI().getPath());
                return handleError(response, "Invalid Authorization header format. Expected: Bearer <token>");
            }

            // Trích xuất JWT token (bỏ prefix "Bearer ")
            String jwtToken = authHeader.substring(7);

            try {
                // Validate JWT token
                if (!jwtService.validateToken(jwtToken)) {
                    log.warn("Invalid or expired JWT token for path: {}", request.getURI().getPath());
                    return handleError(response, "Invalid or expired JWT token");
                }

                // Giải mã token và lấy claims
                Claims claims = jwtService.extractAllClaims(jwtToken);

                // Trích xuất thông tin user từ claims
                String userId = extractClaimAsString(claims, "userId");
                String username = claims.getSubject(); // claim "sub"
                String fullname = extractClaimAsString(claims, "fullname");

                // Log user info
                log.info("Authenticated user - ID: {}, Username: {}, Fullname: {}",
                        userId, username, fullname);

                // Clone request và thêm headers chứa thông tin user
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Name", username != null ? username : "")
                        .header("X-User-Fullname", fullname != null ? fullname : "")
                        .build();

                // Log headers đã inject
                logInjectedHeaders(mutatedRequest);

                // Tạo exchange mới với request đã được mutate
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(mutatedRequest)
                        .build();

                // Tiếp tục chuỗi filter với request đã được thêm headers
                return chain.filter(mutatedExchange);

            } catch (JwtException e) {
                log.error("JWT processing error for path: {} - Error: {}",
                        request.getURI().getPath(), e.getMessage());
                return handleError(response, "JWT token processing error: " + e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error during authentication for path: {} - Error: {}",
                        request.getURI().getPath(), e.getMessage(), e);
                return handleError(response, "Internal authentication error");
            }
        };
    }

    /**
     * Trích xuất claim từ Claims object và chuyển thành String
     *
     * @param claims    Claims object từ JWT
     * @param claimName Tên claim cần trích xuất
     * @return Giá trị claim dưới dạng String, null nếu không tồn tại
     */
    private String extractClaimAsString(Claims claims, String claimName) {
        Object claimValue = claims.get(claimName);
        if (claimValue == null) {
            return null;
        }
        return claimValue.toString();
    }

    /**
     * Log thông tin request khi nhận được
     *
     * @param request ServerHttpRequest
     */
    private void logRequest(ServerHttpRequest request) {
        log.debug("Incoming request - Method: {}, Path: {}, Headers: {}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getHeaders().toSingleValueMap());
    }

    /**
     * Log các headers đã được inject vào request
     *
     * @param request ServerHttpRequest sau khi đã thêm headers
     */
    private void logInjectedHeaders(ServerHttpRequest request) {
        log.debug("Injected headers - X-User-Id: {}, X-User-Name: {}, X-User-Fullname: {}",
                request.getHeaders().getFirst("X-User-Id"),
                request.getHeaders().getFirst("X-User-Name"),
                request.getHeaders().getFirst("X-User-Fullname"));
    }

    /**
     * Xử lý lỗi authentication và trả về response lỗi
     *
     * @param response     ServerHttpResponse
     * @param errorMessage Thông báo lỗi
     * @return Mono<Void> complete signal
     */
    private Mono<Void> handleError(ServerHttpResponse response, String errorMessage) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Tạo error response body
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", errorMessage);
        errorResponse.put("path", "N/A");

        // Chuyển error response thành JSON string
        String errorBody = toJsonString(errorResponse);

        // Ghi error response body
        DataBuffer buffer = response.bufferFactory().wrap(
                errorBody.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Chuyển Map thành JSON string đơn giản
     * Sử dụng StringBuilder thay vì external library để giảm dependency
     *
     * @param map Map cần chuyển thành JSON
     * @return JSON string
     */
    private String toJsonString(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;

            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Escape các ký tự đặc biệt trong JSON string
     *
     * @param str String cần escape
     * @return String đã được escape
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }

        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
