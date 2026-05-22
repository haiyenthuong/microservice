package com.payment.application.dto;

import java.time.LocalDateTime;

/**
 * Generic response class cho tất cả API responses.
 */
public class Response<T> {
    public int status;
    public String message;
    public T data;
    public LocalDateTime timestamp;

    public Response() {
    }

    public Response(int status, String message, T data, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * Tạo response thành công với message và data.
     */
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(200, message, data, LocalDateTime.now());
    }

    /**
     * Tạo response thành công với message mặc định.
     */
    public static <T> Response<T> success(T data) {
        return success("Operation successful", data);
    }

    /**
     * Tạo response lỗi với status và message tùy chỉnh.
     */
    public static <T> Response<T> error(int status, String message) {
        return new Response<>(status, message, null, LocalDateTime.now());
    }

    /**
     * Tạo response lỗi với status mặc định 500.
     */
    public static <T> Response<T> error(String message) {
        return error(500, message);
    }
}
