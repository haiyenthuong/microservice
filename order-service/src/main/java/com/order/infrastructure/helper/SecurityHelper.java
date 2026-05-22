package com.order.infrastructure.helper;

import com.order.infrastructure.security.UserContext;
import com.order.infrastructure.security.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Security Helper - Replacement cho Spring Security
 *
 * Trong kiến trúc cũ: Dùng SecurityContextHolder của Spring Security
 * Trong kiến trúc mới: Dùng UserContextHolder với ThreadLocal
 *
 * Class này cung cấp các utility methods để truy xuất thông tin user
 * từ UserContextHolder (được set bởi UserHeaderInterceptor).
 *
 * Lợi ích:
 * - Không phụ thuộc vào Spring Security
 * - Code đơn giản hơn, dễ test
 * - Thread-safe nhờ ThreadLocal
 * - Không cần mock SecurityContext trong unit tests
 */
@Slf4j
@Component
public class SecurityHelper {

    /**
     * Lấy ID của user đang đăng nhập.
     *
     * User ID được lấy từ header X-User-Id (được set bởi API Gateway)
     *
     * @return User ID (UUID string), hoặc null nếu chưa authenticated
     */
    public String getCurrentUserId() {
        try {
            return UserContextHolder.getUserId();
        } catch (Exception e) {
            log.error("Error extracting current user ID", e);
            return null;
        }
    }

    /**
     * Lấy username của user đang đăng nhập.
     *
     * Username được lấy từ header X-User-Name (được set bởi API Gateway)
     *
     * @return Username, hoặc null nếu chưa authenticated
     */
    public String getCurrentUsername() {
        try {
            return UserContextHolder.getUsername();
        } catch (Exception e) {
            log.error("Error extracting current username", e);
            return null;
        }
    }

    /**
     * Lấy fullname của user đang đăng nhập.
     *
     * Fullname được lấy từ header X-User-Fullname (được set bởi API Gateway)
     *
     * @return Fullname, hoặc null nếu không có trong header
     */
    public String getCurrentFullname() {
        try {
            return UserContextHolder.getFullname();
        } catch (Exception e) {
            log.error("Error extracting current fullname", e);
            return null;
        }
    }

    /**
     * Kiểm tra user hiện tại đã được authenticated hay chưa.
     *
     * User được coi là authenticated nếu cả userId và username đều có giá trị.
     *
     * @return true nếu đã authenticated, ngược lại false
     */
    public boolean isAuthenticated() {
        try {
            return UserContextHolder.isAuthenticated();
        } catch (Exception e) {
            log.error("Error checking authentication status", e);
            return false;
        }
    }

    /**
     * Lấy UserContext đầy đủ của user hiện tại.
     *
     * @return UserContext, hoặc empty context nếu chưa authenticated
     */
    public UserContext getCurrentUserContext() {
        try {
            UserContext context = UserContextHolder.getContext();
            if (context == null) {
                log.debug("UserContext is null, returning empty context");
                return UserContext.empty();
            }
            return context;
        } catch (Exception e) {
            log.error("Error getting current user context", e);
            return UserContext.empty();
        }
    }

    /**
     * Lấy Authorization header từ current request.
     *
     * Lưu ý: Trong kiến trúc mới, JWT token đã được validate bởi API Gateway.
     * Method này chủ yếu dùng khi cần propagate token đến external services
     * (không qua API Gateway).
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

    /**
     * Lấy IP address của client.
     *
     * @return Client IP address, hoặc null nếu không thể xác định
     */
    public String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Check for proxy headers first
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    // X-Forwarded-For can contain multiple IPs, first one is original client
                    return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }

                // Fallback to remote address
                return request.getRemoteAddr();
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting client IP address", e);
            return null;
        }
    }

    /**
     * Kiểm tra xem user hiện tại có phải là user cụ thể không.
     *
     * @param userId User ID cần so sánh
     * @return true nếu user hiện tại khớp với userId đã cho
     */
    public boolean isCurrentUser(String userId) {
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * Require authentication - Throw exception nếu chưa authenticated.
     *
     * Method này hữu ích cho các endpoints bắt buộc phải có authentication.
     *
     * @throws IllegalStateException nếu chưa authenticated
     */
    public void requireAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }
    }

    /**
     * Require specific user - Throw exception nếu user hiện tại không khớp.
     *
     * @param userId User ID bắt buộc
     * @throws IllegalStateException nếu không phải user đã cho
     */
    public void requireUser(String userId) {
        requireAuthenticated();
        if (!isCurrentUser(userId)) {
            throw new IllegalStateException(
                    String.format("Access denied. Required user: %s, Current user: %s",
                            userId, getCurrentUserId()));
        }
    }

    /**
     * Log thông tin user hiện tại (để debug).
     *
     * @return String chứa thông tin user
     */
    public String getCurrentUserInfo() {
        UserContext context = getCurrentUserContext();
        if (context.isAuthenticated()) {
            return String.format("User[%s] (%s) - %s",
                    context.getUserId(),
                    context.getUsername(),
                    context.getFullname() != null ? context.getFullname() : "N/A");
        } else {
            return "Anonymous";
        }
    }
}
