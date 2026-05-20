package com.order.infrastructure.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign RequestInterceptor để propagate Authorization header.
 * Giúp JWT token được tự động truyền sang các service khác khi gọi qua Feign.
 */
@Slf4j
@Component
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null) {
                    template.header("Authorization", authorization);
                    log.debug("Propagated Authorization header to Feign request: {}", template.url());
                }
            }
        } catch (Exception e) {
            log.error("Error propagating Authorization header", e);
        }
    }
}
