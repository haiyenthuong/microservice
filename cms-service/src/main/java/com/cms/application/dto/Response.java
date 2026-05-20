package com.cms.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    /**
     * Tạo response thành công với message và data.
     *
     * @param message thông báo thành công
     * @param data dữ liệu trả về
     * @return Response object với status 200
     */
    public static <T> Response<T> success(String message, T data) {
        return Response.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tạo response thành công với message mặc định.
     *
     * @param data dữ liệu trả về
     * @return Response object với status 200 và message mặc định
     */
    public static <T> Response<T> success(T data) {
        return success("Operation successful", data);
    }

    /**
     * Tạo response lỗi với status và message tùy chỉnh.
     *
     * @param status mã lỗi HTTP
     * @param message thông báo lỗi
     * @return Response object với thông tin lỗi
     */
    public static <T> Response<T> error(int status, String message) {
        return Response.<T>builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tạo response lỗi với status mặc định 500.
     *
     * @param message thông báo lỗi
     * @return Response object với status 500
     */
    public static <T> Response<T> error(String message) {
        return error(500, message);
    }
}
