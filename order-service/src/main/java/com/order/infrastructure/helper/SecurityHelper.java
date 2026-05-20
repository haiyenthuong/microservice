package com.order.infrastructure.helper;

import com.order.infrastructure.config.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Helper class xử lý các tác vụ liên quan đến Security.
 * Copy từ cms-service với bổ sung getAuthorizationHeader.
 */
@Slf4j
@Component
public class SecurityHelper {

    /**
     * Lấy ID của người dùng đang đăng nhập.
     *
     * @return ID của người dùng đang đăng nhập, hoặc null nếu không có
     */
    public String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No authentication found in SecurityContext");
                return null;
            }

            Object principal = authentication.getPrincipal();

            // Principal là CustomUserDetails (được set trong JwtAuthenticationFilter)
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }

            log.debug("Principal is not CustomUserDetails: {}", principal.getClass().getName());
            return null;

        } catch (Exception e) {
            log.error("Error extracting current user ID", e);
            return null;
        }
    }

    /**
     * Lấy username của người dùng đang đăng nhập.
     *
     * @return username, hoặc null nếu không có
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Kiểm tra người dùng hiện tại đã được authenticate hay chưa.
     *
     * @return true nếu đã authenticated, ngược lại false
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Lấy Authorization header từ current request.
     * Dùng để propagate JWT token khi gọi external services qua Feign.
     *
     * @return Authorization header value (Bearer token), hoặc null nếu không có
     */
    public String getAuthorizationHeader() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("Authorization");
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting authorization header", e);
            return null;
        }
    }
}
