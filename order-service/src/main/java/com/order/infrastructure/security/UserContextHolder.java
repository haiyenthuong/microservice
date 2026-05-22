package com.order.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserContextHolder - Quản lý UserContext trong ThreadLocal
 *
 * Class này cung cấp thread-safe storage cho UserContext trong suốt lifecycle của một request.
 * Mỗi request sẽ có UserContext riêng của nó, được lưu trong ThreadLocal.
 *
 * Lifecycle:
 * 1. UserHeaderInterceptor bắt headers từ API Gateway và set UserContext vào ThreadLocal
 * 2. Các tầng Service/Command/Query truy xuất UserContext từ ThreadLocal
 * 3. UserHeaderInterceptor clears UserContext sau khi request hoàn thành
 *
 * Lưu ý quan trọng:
 * - LUÔN clear context sau khi request xong để tránh memory leak
 * - KHÔNG dùng UserContextHolder trong async threads (thread khác sẽ không có context)
 * - Chỉ sử dụng trong scope của một HTTP request
 */
@Slf4j
@Component
public class UserContextHolder {

    /**
     * ThreadLocal để lưu UserContext cho mỗi thread
     *
     * Sử dụng ThreadLocal.withInitial() để đảm bảo mỗi thread có một empty UserContext ban đầu.
     * Điều này tránh NullPointerException khi gọi get() mà chưa set().
     */
    private static final ThreadLocal<UserContext> CONTEXTHolder = ThreadLocal.withInitial(UserContext::empty);

    /**
     * Lấy UserContext của thread hiện tại
     *
     * @return UserContext của thread hiện tại, có thể empty nếu chưa được set
     */
    public static UserContext getContext() {
        return CONTEXTHolder.get();
    }

    /**
     * Set UserContext cho thread hiện tại
     *
     * @param userContext UserContext cần set
     */
    public static void setContext(UserContext userContext) {
        if (userContext == null) {
            log.warn("Attempted to set null UserContext, ignoring");
            return;
        }
        CONTEXTHolder.set(userContext);
        log.debug("UserContext set for thread: {} - User: {} ({})",
                Thread.currentThread().getName(),
                userContext.getUsername(),
                userContext.getUserId());
    }

    /**
     * Clear UserContext của thread hiện tại
     *
     * Lưu ý: Nên gọi method này trong finally block của interceptor/filter
     * để đảm bảo context luôn được clear dù có exception hay không.
     */
    public static void clearContext() {
        UserContext context = CONTEXTHolder.get();
        if (context != null && context.isAuthenticated()) {
            log.debug("Clearing UserContext for user: {} ({})",
                    context.getUsername(), context.getUserId());
        }
        CONTEXTHolder.remove();
    }

    /**
     * Kiểm tra xem thread hiện tại có authenticated user context không
     *
     * @return true nếu UserContext đã được set và có thông tin user hợp lệ
     */
    public static boolean isAuthenticated() {
        return getContext().isAuthenticated();
    }

    /**
     * Lấy userId của user hiện tại
     *
     * @return userId, hoặc null nếu chưa authenticated
     */
    public static String getUserId() {
        return getContext().getUserId();
    }

    /**
     * Lấy username của user hiện tại
     *
     * @return username, hoặc null nếu chưa authenticated
     */
    public static String getUsername() {
        return getContext().getUsername();
    }

    /**
     * Lấy fullname của user hiện tại
     *
     * @return fullname, hoặc null nếu chưa có trong context
     */
    public static String getFullname() {
        return getContext().getFullname();
    }

    /**
     * Set UserContext từ userId và username
     *
     * @param userId   User ID
     * @param username Username
     */
    public static void setContext(String userId, String username) {
        setContext(UserContext.of(userId, username));
    }

    /**
     * Set UserContext đầy đủ từ userId, username và fullname
     *
     * @param userId   User ID
     * @param username Username
     * @param fullname Fullname
     */
    public static void setContext(String userId, String username, String fullname) {
        setContext(UserContext.of(userId, username, fullname));
    }
}
