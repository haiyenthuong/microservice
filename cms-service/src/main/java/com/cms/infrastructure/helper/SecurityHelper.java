package com.cms.infrastructure.helper;

import com.cms.infrastructure.config.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper class xử lý các tác vụ liên quan đến Security.
 *
 * <p>Cung cấp các utility method để làm việc với Authentication,
 * JWT token và thông tin người dùng hiện tại.</p>
 */
@Slf4j
@Component
public class SecurityHelper {

    /**
     * Lấy ID của người dùng đang đăng nhập.
     *
     * <p>Method này thực hiện các bước:
     * <ol>
     *   <li>Lấy Authentication từ SecurityContextHolder</li>
     *   <li>Cast principal sang CustomUserDetails</li>
     *   <li>Extract userId từ CustomUserDetails</li>
     * </ol>
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
}
