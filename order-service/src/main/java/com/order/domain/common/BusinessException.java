package com.order.domain.common;

/**
 * Exception được throw khi vi phạm luật kinh doanh.
 * Exception này được sử dụng cho các lỗi liên quan đến business logic.
 */
public class BusinessException extends BaseException {

    /**
     * Khởi tạo business exception với thông báo.
     *
     * @param message thông báo lỗi
     */
    public BusinessException(String message) {
        super(400, message);
    }

    /**
     * Khởi tạo business exception với thông báo và nguyên nhân.
     *
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc rễ
     */
    public BusinessException(String message, Throwable cause) {
        super(400, message, cause);
    }

    /**
     * Khởi tạo business exception với mã code và thông báo.
     *
     * @param code mã lỗi HTTP
     * @param message thông báo lỗi
     */
    public BusinessException(int code, String message) {
        super(code, message);
    }
}
