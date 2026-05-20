package com.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client để gọi order-service.
 * Dùng để callback cập nhật trạng thái thanh toán.
 */
@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8082}/order-service")
public interface OrderServiceClient {

    /**
     * Cập nhật trạng thái thanh toán cho order.
     *
     * @param orderId ID của đơn hàng
     * @param request request chứa thông tin cập nhật
     * @param authorization JWT token
     */
    @PatchMapping("/v1/orders/{orderId}/payment-status")
    void updatePaymentStatus(@PathVariable String orderId,
                            @RequestBody PaymentStatusUpdateRequest request,
                            @RequestHeader("Authorization") String authorization);

    /**
     * Request DTO để cập nhật trạng thái thanh toán.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PaymentStatusUpdateRequest {
        private String paymentStatus; // PAID, FAILED
        private String transactionId;
        private String failureReason;
    }
}
