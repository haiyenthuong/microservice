package com.cms.domain.common;

public class ResourceNotFoundException extends BaseException {
    /**
     * Khởi tạo exception với thông báo tùy chỉnh.
     *
     * @param message thông báo lỗi
     */
    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    /**
     * Khởi tạo exception với thông tin tài nguyên không tìm thấy theo ID.
     *
     * @param resource tên tài nguyên
     * @param id ID của tài nguyên
     */
    public ResourceNotFoundException(String resource, String id) {
        super(404, String.format("%s not found with id: %s", resource, id));
    }

    /**
     * Khởi tạo exception với thông tin tài nguyên không tìm thấy theo trường.
     *
     * @param resource tên tài nguyên
     * @param field tên trường
     * @param value giá trị của trường
     */
    public ResourceNotFoundException(String resource, String field, String value) {
        super(404, String.format("%s not found with %s: %s", resource, field, value));
    }
}
