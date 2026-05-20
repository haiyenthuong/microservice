package com.order.domain.common;

/**
 * Exception được throw khi không tìm thấy resource được yêu cầu.
 * Exception này được sử dụng khi resource không tồn tại trong hệ thống.
 */
public class ResourceNotFoundException extends BaseException {

    /**
     * Khởi tạo exception với tên resource, field và giá trị.
     *
     * @param resource tên resource (ví dụ: "Order", "OrderItem")
     * @param field tên field (ví dụ: "id", "orderNumber")
     * @param value giá trị tìm kiếm
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(404, String.format("%s not found with %s: '%s'", resource, field, value));
    }

    /**
     * Khởi tạo exception với thông báo tùy chỉnh.
     *
     * @param message thông báo lỗi
     */
    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    /**
     * Khởi tạo exception với tên resource và ID.
     *
     * @param resource tên resource
     * @param id ID của resource
     */
    public ResourceNotFoundException(String resource, String id) {
        this(resource, "id", id);
    }
}
