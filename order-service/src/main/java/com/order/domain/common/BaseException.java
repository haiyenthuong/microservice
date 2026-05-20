package com.order.domain.common;

/**
 * Base exception class cho tất cả application exceptions.
 * Lớp exception cơ sở cho mọi exception trong ứng dụng.
 */
public class BaseException extends RuntimeException {
    private final int code;

    /**
     * Khởi tạo exception với mã code và thông báo.
     *
     * @param code mã lỗi HTTP
     * @param message thông báo lỗi
     */
    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Khởi tạo exception với thông báo (mặc định code 500).
     *
     * @param message thông báo lỗi
     */
    public BaseException(String message) {
        this(500, message);
    }

    /**
     * Khởi tạo exception với thông báo và nguyên nhân (mặc định code 500).
     *
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc rễ của exception
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    /**
     * Khởi tạo exception với mã code, thông báo và nguyên nhân.
     *
     * @param code mã lỗi HTTP
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc rễ của exception
     */
    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Lấy mã lỗi HTTP của exception.
     *
     * @return mã lỗi HTTP
     */
    public int getCode() {
        return code;
    }
}
