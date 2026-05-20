package com.cms.domain.common;

public class AuthenticationException extends BaseException {
    /**
     * Khởi tạo exception với thông báo tùy chỉnh.
     *
     * @param message thông báo lỗi
     */
    public AuthenticationException(String message) {
        super(401, message);
    }

    /**
     * Khởi tạo exception với thông báo mặc định.
     */
    public AuthenticationException() {
        super(401, "Authentication failed");
    }
}
