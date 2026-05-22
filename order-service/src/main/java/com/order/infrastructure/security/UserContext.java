package com.order.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserContext - Object chứa thông tin user được truyền từ API Gateway
 *
 * Thông tin này được trích xuất từ HTTP Headers:
 * - X-User-Id: ID của user (UUID)
 * - X-User-Name: Username của user
 * - X-User-Fullname: Tên đầy đủ của user
 *
 * Object này được lưu trong ThreadLocal để có thể truy cập từ bất kỳ đâu trong request scope.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    /**
     * ID của user - được lấy từ header X-User-Id
     * Đây là UUID định danh duy nhất cho user
     */
    private String userId;

    /**
     * Username của user - được lấy từ header X-User-Name
     * Đây là username dùng để đăng nhập
     */
    private String username;

    /**
     * Tên đầy đủ của user - được lấy từ header X-User-Fullname
     * Có thể null nếu không có trong header
     */
    private String fullname;

    /**
     * Kiểm tra xem user context có được set hay chưa
     *
     * @return true nếu userId và username đều không null
     */
    public boolean isAuthenticated() {
        return userId != null && !userId.isEmpty()
                && username != null && !username.isEmpty();
    }

    /**
     * Tạo empty UserContext
     *
     * @return UserContext với tất cả fields là null
     */
    public static UserContext empty() {
        return new UserContext(null, null, null);
    }

    /**
     * Tạo UserContext từ userId và username
     *
     * @param userId   User ID
     * @param username Username
     * @return UserContext mới
     */
    public static UserContext of(String userId, String username) {
        return new UserContext(userId, username, null);
    }

    /**
     * Tạo UserContext đầy đủ từ userId, username và fullname
     *
     * @param userId   User ID
     * @param username Username
     * @param fullname Fullname
     * @return UserContext mới
     */
    public static UserContext of(String userId, String username, String fullname) {
        return new UserContext(userId, username, fullname);
    }
}
