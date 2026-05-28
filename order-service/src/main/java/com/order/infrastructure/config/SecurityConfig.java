package com.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình security cho order-service.
 *
 * Trong kiến trúc microservices với API Gateway:
 * - JWT validation được xử lý tại API Gateway
 * - Gateway inject user info qua headers (X-User-Id, X-User-Name, X-User-Fullname)
 * - Service này trust headers từ gateway và không validate JWT lại
 *
 * @author Order Service
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Cấu hình security - permissive mode cho microservice.
     *
     * Tất cả requests được allow vì authentication được xử lý bởi API Gateway.
     * User context được lấy từ headers được inject bởi Gateway (xem CurrentUserHelper).
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception nếu có lỗi
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
