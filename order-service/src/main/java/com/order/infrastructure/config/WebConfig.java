package com.order.infrastructure.config;

import com.order.infrastructure.security.UserHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration cho Order Service
 *
 * Cấu hình interceptor để xử lý user headers từ API Gateway.
 *
 * Đây là replacement cho SecurityConfig trong kiến trúc cũ:
 * - Không sử dụng Spring Security anymore
 * - Thay vào đó, trust headers từ API Gateway
 * - UserHeaderInterceptor trích xuất user info từ headers
 *
 * Paths excluded (không cần authenticated):
 * - /actuator/**: Health check, metrics, etc.
 * - /v3/api-docs/**: Swagger API docs
 * - /swagger-ui/**: Swagger UI
 * - /public/**: Public APIs (nếu có)
 *
 * Các paths còn lại sẽ có UserContext được set từ headers (nếu có).
 * Nếu không có headers, UserContext sẽ empty.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserHeaderInterceptor userHeaderInterceptor;

    /**
     * Đăng ký UserHeaderInterceptor cho tất cả requests
     *
     * Interceptor sẽ:
     * 1. Trích xuất X-User-Id, X-User-Name, X-User-Fullname từ headers
     * 2. Set vào UserContextHolder (ThreadLocal)
     * 3. Clear sau khi request hoàn thành
     *
     * Note: KHÔNG exclude paths ở đây.
     * Tất cả requests đều đi qua interceptor để headers được extracted.
     * Controller sẽ tự quyết định xem endpoint có cần authentication hay không.
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(userHeaderInterceptor)
                .addPathPatterns("/**")  // Áp dụng cho tất cả paths
                .order(1);  // Thứ tự ưu tiên (1 = cao)
    }
}
