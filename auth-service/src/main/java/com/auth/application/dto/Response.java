package com.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic Response DTO cho tất cả API responses
 *
 * Cung cấp format response nhất quán:
 * - status: HTTP status code
 * - message: Thông báo về kết quả
 * - data: Dữ liệu trả về (nếu có)
 * - timestamp: Thời gian server
 * - path: Request path (cho error cases)
 *
 * @param <T> Type của data field
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response")
public class Response<T> {

    @Schema(description = "HTTP status code", example = "200")
    private Integer status;

    @Schema(description = "Response message", example = "Operation successful")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Server timestamp", example = "1716600000000")
    private Long timestamp;

    @Schema(description = "Request path (for errors)")
    private String path;

    /**
     * Tạo success response với data
     *
     * @param message  Message thành công
     * @param data     Dữ liệu trả về
     * @param <T>      Type của data
     * @return Response object
     */
    public static <T> Response<T> success(String message, T data) {
        return Response.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Tạo success response với message only
     *
     * @param message Message thành công
     * @param <T>     Type của data
     * @return Response object
     */
    public static <T> Response<T> success(String message) {
        return Response.<T>builder()
                .status(200)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Tạo success response với data và message mặc định
     *
     * @param data Dữ liệu trả về
     * @param <T>   Type của data
     * @return Response object
     */
    public static <T> Response<T> of(T data) {
        return Response.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Tạo error response
     *
     * @param status    HTTP status code
     * @param message   Error message
     * @param path      Request path
     * @param <T>       Type của data
     * @return Response object
     */
    public static <T> Response<T> error(Integer status, String message, String path) {
        return Response.<T>builder()
                .status(status)
                .message(message)
                .path(path)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
