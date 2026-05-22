package com.order.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * UserHeaderInterceptor - Interceptor để bắt User Headers từ API Gateway
 *
 * Mỗi request từ API Gateway sẽ chứa các headers sau:
 * - X-User-Id: UUID của user
 * - X-User-Name: Username của user
 * - X-User-Fullname: Tên đầy đủ của user (optional)
 *
 * Interceptor này:
 * 1. Trích xuất thông tin user từ headers
 * 2. Lưu vào UserContextHolder (ThreadLocal)
 * 3. Clear context sau khi request hoàn thành
 *
 * Điều này cho phép các tầng Service/Command/Query truy cập thông tin user
 * mà không cần truyền parameters qua nhiều lớp.
 *
 * Thứ tự thực hiện:
 * 1. preHandle() - Được gọi BEFORE controller method
 *    → Extract headers → Set UserContext
 *
 * 2. Controller/Service/Command/Query xử lý business logic
 *    → Có thể truy cập UserContext từ UserContextHolder
 *
 * 3. afterCompletion() - Được gọi AFTER view rendering
 *    → Clear UserContext để tránh memory leak
 */
@Slf4j
@Component
public class UserHeaderInterceptor implements HandlerInterceptor {

    /**
     * Header names được gửi từ API Gateway
     */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_FULLNAME = "X-User-Fullname";

    /**
     * Lưu ý: Các headers này có thể có trong lowercase (http/1.1) hoặc mixed case (http/2)
     * Nên xử lý case-insensitive khi trích xuất.
     */

    /**
     * Pre-handle method - Được gọi BEFORE controller method
     *
     * Trích xuất user headers từ request và set vào UserContextHolder.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler Handler được chọn
     * @return true để continue chain, false để stop processing
     * @throws Exception nếu có lỗi
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        // Extract headers case-insensitively
        String userId = extractHeader(request, HEADER_USER_ID);
        String username = extractHeader(request, HEADER_USER_NAME);
        String fullname = extractHeader(request, HEADER_USER_FULLNAME);

        // Log incoming request với user info
        logRequest(request, userId, username);

        // Validate rằng các headers cần thiết có mặt
        if (userId == null || userId.isEmpty()) {
            log.warn("Missing X-User-Id header in request: {} {}",
                    request.getMethod(), request.getRequestURI());
            // Không reject request ở đây - có thể là public endpoint
            // UserContext sẽ empty và controller có thể handle accordingly
        }

        if (username == null || username.isEmpty()) {
            log.warn("Missing X-User-Name header in request: {} {}",
                    request.getMethod(), request.getRequestURI());
        }

        // Set UserContext vào ThreadLocal
        if (userId != null && username != null) {
            UserContextHolder.setContext(userId, username, fullname);
            log.debug("UserContext set for user: {} ({})", username, userId);
        } else {
            // Set empty context để tránh NullPointerException
            UserContextHolder.setContext(UserContext.empty());
            log.debug("Empty UserContext set (missing user headers)");
        }

        // Continue chain
        return true;
    }

    /**
     * After-completion method - Được gọi AFTER request hoàn thành
     *
     * Clear UserContext từ ThreadLocal để tránh memory leak.
     * Method này LUÔN được gọi dù có exception hay không.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler Handler được chọn
     * @param ex      Exception nếu có (null nếu không có lỗi)
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {

        try {
            // Clear UserContext từ ThreadLocal
            UserContext context = UserContextHolder.getContext();
            if (context != null && context.isAuthenticated()) {
                log.debug("Clearing UserContext for user: {} ({})",
                        context.getUsername(), context.getUserId());
            }
            UserContextHolder.clearContext();

        } catch (Exception e) {
            // Log nhưng không throw exception trong after-completion
            log.error("Error clearing UserContext", e);
        }
    }

    /**
     * Trích xuất header từ request case-insensitively
     *
     * @param request     HttpServletRequest
     * @param headerName  Tên header cần trích xuất
     * @return Giá trị header, hoặc null nếu không tồn tại
     */
    private String extractHeader(HttpServletRequest request, String headerName) {
        // Spring's getHeader() already handles case-insensitivity
        return request.getHeader(headerName);
    }

    /**
     * Log request info với user context
     *
     * @param request  HttpServletRequest
     * @param userId   User ID từ header
     * @param username Username từ header
     */
    private void logRequest(HttpServletRequest request, String userId, String username) {
        log.info("=== INCOMING REQUEST ===");
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getRequestURI());
        log.info("Query String: {}", request.getQueryString());
        log.info("Remote Addr: {}", request.getRemoteAddr());
        log.info("User ID: {}", userId != null ? userId : "N/A");
        log.info("Username: {}", username != null ? username : "N/A");
        log.info("========================");
    }
}
