package com.auth.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration cho Auth Service
 *
 * Cấu hình:
 * - Password encoding (BCrypt)
 * - Authentication manager
 * - Security filter chain
 * - Method security (@PreAuthorize, @Secured)
 * - CORS configuration
 * - Public endpoints: /auth/login, /auth/register
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security
     *
     * Public endpoints (không cần auth):
     * - /auth/login, /auth/register
     * - /actuator/** (health check)
     * - /swagger-ui/**, /api-docs/**
     *
     * All other endpoints require authentication
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF cho stateless APIs
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (đã strip prefix tại gateway)
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // Không add JwtFilter vì JWT validation sẽ được done ở API Gateway
        // Auth Service chỉ cần validate basic auth hoặc không cần validate (đã qua
        // gateway)

        return http.build();
    }

    /**
     * CORS Configuration source
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Password Encoder bean
     *
     * Sử dụng BCryptPasswordEncoder với strength 10
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
