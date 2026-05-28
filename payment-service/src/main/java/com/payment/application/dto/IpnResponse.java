package com.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho kết quả xử lý IPN từ VNPAY.
 * VNPAY yêu cầu response format: { "RspCode": "00", "Message": "Confirm Success" }
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpnResponse {

    /**
     * Response code cho VNPAY
     * - 00: Thành công
     * - Others: Thất bại
     */
    private String RspCode;

    /**
     * Message mô tả kết quả
     */
    private String Message;

    /**
     * Tạo response thành công theo format VNPAY yêu cầu
     */
    public static IpnResponse success(String message) {
        return IpnResponse.builder()
                .RspCode("00")
                .Message(message)
                .build();
    }

    /**
     * Tạo response lỗi
     */
    public static IpnResponse error(String message) {
        return IpnResponse.builder()
                .RspCode("99")
                .Message(message)
                .build();
    }
}
