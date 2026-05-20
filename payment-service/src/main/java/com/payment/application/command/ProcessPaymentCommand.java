package com.payment.application.command;

import com.payment.application.dto.PaymentResponse;
import com.payment.application.dto.ProcessPaymentRequest;
import com.payment.domain.model.Payment;
import com.payment.domain.model.PaymentStatus;
import com.payment.domain.repository.PaymentRepository;
import com.payment.infrastructure.client.OrderServiceClient;
import com.payment.infrastructure.client.OrderServiceClient.PaymentStatusUpdateRequest;
import com.payment.infrastructure.helper.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command để xử lý thanh toán.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessPaymentCommand implements ICommand {

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final SecurityHelper securityHelper;

    /**
     * Execute command để xử lý thanh toán.
     *
     * @param request DTO chứa thông tin thanh toán
     * @return PaymentResponse
     */
    @Transactional
    public PaymentResponse execute(ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Tạo payment với status PROCESSING
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentStatus(PaymentStatus.PROCESSING.getValue())
                .paymentMethod(request.getPaymentMethod())
                .build();

        paymentRepository.save(payment);

        // Giả lập xử lý payment (hoặc gọi payment gateway thật)
        boolean paymentSuccess = processPaymentGateway(payment);

        // Cập nhật status
        if (paymentSuccess) {
            payment.markAsPaid(generateTransactionId());
            log.info("Payment successful for order: {}, transaction ID: {}", request.getOrderId(), payment.getTransactionId());
        } else {
            payment.markAsFailed("Payment gateway declined");
            log.warn("Payment failed for order: {}", request.getOrderId());
        }

        paymentRepository.save(payment);

        // Callback sang order-service để cập nhật trạng thái
        try {
            PaymentStatusUpdateRequest updateRequest = PaymentStatusUpdateRequest.builder()
                    .paymentStatus(paymentSuccess ? "PAID" : "FAILED")
                    .transactionId(payment.getTransactionId())
                    .failureReason(payment.getFailureReason())
                    .build();

            String authorization = securityHelper.getAuthorizationHeader();
            orderServiceClient.updatePaymentStatus(request.getOrderId(), updateRequest, authorization);
            log.info("Successfully updated payment status for order: {}", request.getOrderId());
        } catch (Exception e) {
            log.error("Failed to callback order-service for order: {}", request.getOrderId(), e);
            // Không throw exception vì payment đã được xử lý
        }

        return mapToResponse(payment);
    }

    /**
     * Giả lập xử lý payment gateway.
     * Trong thực tế, đây là nơi gọi API của các payment gateway như Stripe, PayPal, v.v.
     */
    private boolean processPaymentGateway(Payment payment) {
        // Giả lập: 80% thành công, 20% thất bại
        // Trong production, thay bằng logic gọi payment gateway thật
        double random = Math.random();
        log.debug("Payment gateway simulation result: {} (threshold: 0.8)", random);
        return random < 0.8;
    }

    /**
     * Tạo transaction ID duy nhất.
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map Payment entity sang PaymentResponse DTO.
     */
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentStatus status = payment.getPaymentStatusEnum();

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus())
                .paymentStatusName(status.getName())
                .paymentStatusDescription(status.getDescription())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .failureReason(payment.getFailureReason())
                .createdDate(payment.getCreatedDate())
                .updatedDate(payment.getUpdatedDate())
                .build();
    }
}
