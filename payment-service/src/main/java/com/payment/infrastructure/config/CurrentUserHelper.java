package com.payment.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Helper class để extract thông tin user từ headers
 * được inject bởi API Gateway sau khi JWT đã được validated.
 *
 * Headers từ Gateway:
 * - X-User-Id: User ID
 * - X-User-Name: Username
 * - X-User-Fullname: Full name
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Slf4j
@Component
public class CurrentUserHelper {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_FULLNAME = "X-User-Fullname";

    /**
     * Lấy User ID từ request header.
     *
     * @return User ID hoặc empty nếu không có
     */
    public Optional<String> getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.warn("No current HTTP request found");
            return Optional.empty();
        }

        String userId = request.getHeader(HEADER_USER_ID);
        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id header is missing");
            return Optional.empty();
        }

        return Optional.of(userId);
    }

    /**
     * Lấy Username từ request header.
     *
     * @return Username hoặc empty nếu không có
     */
    public Optional<String> getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Optional.empty();
        }

        String username = request.getHeader(HEADER_USER_NAME);
        if (username == null || username.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(username);
    }

    /**
     * Lấy Fullname từ request header.
     *
     * @return Fullname hoặc empty nếu không có
     */
    public Optional<String> getCurrentUserFullname() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Optional.empty();
        }

        String fullname = request.getHeader(HEADER_USER_FULLNAME);
        if (fullname == null || fullname.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(fullname);
    }

    /**
     * Kiểm tra user đã authenticated chưa (có X-User-Id header).
     *
     * @return true nếu đã authenticated, false nếu không
     */
    public boolean isAuthenticated() {
        return getCurrentUserId().isPresent();
    }

    /**
     * Lấy HttpServletRequest hiện tại.
     *
     * @return HttpServletRequest hoặc null nếu không có request context
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        return attributes.getRequest();
    }

    /**
     * Get User ID hoặc throw exception nếu chưa authenticated.
     *
     * @return User ID
     * @throws IllegalStateException nếu chưa authenticated
     */
    public String getRequiredUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
    }

    /**
     * Get Username hoặc throw exception nếu chưa authenticated.
     *
     * @return Username
     * @throws IllegalStateException nếu chưa authenticated
     */
    public String getRequiredUsername() {
        return getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
    }
}
