package com.cms.domain.common;

public class DuplicateResourceException extends BaseException {
    /**
     * Khởi tạo exception với thông báo tùy chỉnh.
     *
     * @param message thông báo lỗi
     */
    public DuplicateResourceException(String message) {
        super(409, message);
    }

    /**
     * Khởi tạo exception với thông tin tài nguyên đã tồn tại.
     *
     * @param resource tên tài nguyên
     * @param field tên trường
     * @param value giá trị của trường
     */
    public DuplicateResourceException(String resource, String field, String value) {
        super(409, String.format("%s already exists with %s: %s", resource, field, value));
    }
}
