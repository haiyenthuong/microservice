package com.cms.domain.common;

public class BusinessException extends BaseException {
    /**
     * Khởi tạo exception với thông báo tùy chỉnh.
     *
     * @param message thông báo lỗi
     */
    public BusinessException(String message) {
        super(400, message);
    }

    /**
     * Khởi tạo exception với thông báo và nguyên nhân.
     *
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc rễ của exception
     */
    public BusinessException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
