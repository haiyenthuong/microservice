package com.order.infrastructure.client;

import com.order.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client để gọi payment-service.
 * Dùng để xử lý thanh toán cho order.
 */
@FeignClient(name = "payment-service", url = "${payment-service.url:http://localhost:8083}/payment-service", configuration = FeignConfig.class)
public interface PaymentServiceClient {

    /**
     * Xử lý thanh toán.
     *
     * @param request request chứa thông tin thanh toán
     * @param authorization JWT token
     * @return kết quả xử lý thanh toán
     */
    @PostMapping("/v1/payments/process")
    PaymentResponse processPayment(@RequestBody ProcessPaymentRequest request,
                                   @RequestHeader("Authorization") String authorization);

    /**
     * Request DTO để xử lý thanh toán.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class ProcessPaymentRequest {
        private String orderId;
        private String userId;
        private java.math.BigDecimal amount;
        private String currency;
        private String paymentMethod;
    }

    /**
     * Response DTO cho thanh toán.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PaymentResponse {
        private String id;
        private String orderId;
        private Integer paymentStatus;
        private String paymentStatusName;
        private String transactionId;
        private String failureReason;
    }
}
