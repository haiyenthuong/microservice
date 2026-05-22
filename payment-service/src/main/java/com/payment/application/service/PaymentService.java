package com.payment.application.service;

import com.payment.domain.entity.Payment;
import com.payment.domain.enums.PaymentProvider;
import com.payment.domain.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Service xử lý logic thanh toán
 *
 * Giả lập kết nối với Payment Gateway (Ngân hàng, VNPAY, MoMo, etc.)
 * với tỷ lệ thành công configurable.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    private final Random random = new Random();

    @Value("${payment.simulation.success-rate:0.8}")
    private double successRate;

    @Value("${payment.simulation.processing-time-ms:2000}")
    private long processingTimeMs;

    @Value("${payment.simulation.enabled:true}")
    private boolean simulationEnabled;

    /**
     * Xử lý thanh toán - Giả lập gọi Payment Gateway
     *
     * Process:
     * 1. Giả lập thời gian xử lý (network latency)
     * 2. Random kết quả thành công/thất bại dựa trên success-rate
     * 3. Nếu thành công: sinh transaction ID, update status PAID
     * 4. Nếu thất bại: ghi nhận failure reason, update status FAILED
     * 5. Lưu kết quả vào database
     *
     * @param payment Payment entity với trạng thái PROCESSING
     * @return Payment entity sau khi xử lý (PAID hoặc FAILED)
     */
    @Transactional
    public Payment processPayment(Payment payment) {
        log.info("Processing payment {} for order {} with amount {} {}",
            payment.paymentNumber, payment.orderId,
            payment.amount, payment.currency);

        try {
            // Giả lập thời gian xử lý của Gateway (network latency)
            simulateProcessingDelay();

            // Xác định kết quả thanh toán (random dựa trên success-rate)
            boolean isSuccess = simulatePaymentGateway(payment);

            if (isSuccess) {
                // Thanh toán thành công
                handlePaymentSuccess(payment);
            } else {
                // Thanh toán thất bại
                handlePaymentFailure(payment);
            }

            // Lưu kết quả
            Payment result = paymentRepository.save(payment);
            log.info("Payment {} completed with status: {}",
                payment.paymentNumber, payment.paymentStatus);

            return result;

        } catch (Exception e) {
            log.error("Error processing payment {}: {}", payment.paymentNumber, e.getMessage(), e);
            handlePaymentException(payment, e);
            return paymentRepository.save(payment);
        }
    }

    /**
     * Giả lập thời gian xử lý của Payment Gateway
     * Network latency: 500ms - 3000ms (configurable)
     */
    private void simulateProcessingDelay() {
        try {
            long delay = processingTimeMs / 2 + random.nextInt((int) processingTimeMs);
            log.debug("Simulating Gateway processing delay: {}ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Processing delay interrupted");
        }
    }

    /**
     * Giả lập gọi Payment Gateway
     *
     * Random kết quả dựa trên success-rate:
     * - successRate = 0.8 => 80% thành công, 20% thất bại
     * - Có thể fail theo các cách khác nhau để mô phỏng thực tế
     *
     * @param payment Payment entity
     * @return true nếu thành công, false nếu thất bại
     */
    private boolean simulatePaymentGateway(Payment payment) {
        if (!simulationEnabled) {
            log.info("Simulation disabled, forcing success for payment: {}", payment.paymentNumber);
            return true;
        }

        double randomValue = random.nextDouble();
        boolean isSuccess = randomValue < successRate;

        log.info("Gateway simulation result for payment {}: {} (threshold: {}, actual: {})",
            payment.paymentNumber,
            isSuccess ? "SUCCESS" : "FAILED",
            successRate,
            String.format("%.3f", randomValue));

        return isSuccess;
    }

    /**
     * Xử lý khi thanh toán thành công
     *
     * - Sinh transaction ID unique
     * - Update status = PAID
     * - Set paidAt timestamp
     * - Lưu gateway response (mock)
     *
     * @param payment Payment entity
     */
    private void handlePaymentSuccess(Payment payment) {
        String transactionId = generateTransactionId(payment);
        String gatewayTransactionId = generateGatewayTransactionId(payment.paymentProvider);
        String gatewayResponse = buildSuccessGatewayResponse(payment);

        payment.markAsPaid(transactionId, gatewayTransactionId, gatewayResponse);

        log.info("Payment SUCCESS: {} - Transaction ID: {}, Gateway TXN ID: {}",
            payment.paymentNumber, transactionId, gatewayTransactionId);
    }

    /**
     * Xử lý khi thanh toán thất bại
     *
     * - Xác định nguyên nhân thất bại (random scenario)
     * - Update status = FAILED
     * - Set failureReason và errorCode
     *
     * @param payment Payment entity
     */
    private void handlePaymentFailure(Payment payment) {
        PaymentFailureScenario scenario = determineFailureScenario();

        payment.markAsFailed(scenario.getReason(), scenario.getErrorCode());

        log.warn("Payment FAILED: {} - Reason: {}, Error Code: {}",
            payment.paymentNumber, scenario.getReason(), scenario.getErrorCode());
    }

    /**
     * Xử lý exception trong quá trình thanh toán
     *
     * @param payment Payment entity
     * @param e Exception
     */
    private void handlePaymentException(Payment payment, Exception e) {
        payment.markAsFailed(
            "System error: " + e.getMessage(),
            "SYSTEM_ERROR"
        );
        log.error("Payment EXCEPTION: {} - Error: {}", payment.paymentNumber, e.getMessage());
    }

    /**
     * Sinh Transaction ID unique cho payment thành công
     * Format: TXN-YYYYMMDDHHMMSS-XXXXXXXX
     *
     * @param payment Payment entity
     * @return Transaction ID string
     */
    private String generateTransactionId(Payment payment) {
        String timestamp = LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        );
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("TXN-%s-%s", timestamp, uniqueId);
    }

    /**
     * Sinh Gateway Transaction ID
     *
     * @param provider PaymentProvider
     * @return Gateway transaction ID
     */
    private String generateGatewayTransactionId(PaymentProvider provider) {
        String prefix = switch (provider) {
            case VNPAY -> "VN";
            case MOMO -> "MO";
            case ZALOPAY -> "ZL";
            case STRIPE -> "ST";
            case PAYPAL -> "PP";
            default -> "GW";
        };
        return prefix + "-" + System.currentTimeMillis() + "-" +
               UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Build Gateway response JSON cho payment thành công (mock)
     *
     * @param payment Payment entity
     * @return JSON string
     */
    private String buildSuccessGatewayResponse(Payment payment) {
        return String.format(
            "{\"status\":\"success\",\"transactionId\":\"%s\",\"amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
            payment.transactionId,
            payment.amount.toString(),
            payment.currency,
            LocalDateTime.now().toString()
        );
    }

    /**
     * Xác định scenario thất bại để mô phỏng các lỗi thực tế
     *
     * @return PaymentFailureScenario
     */
    private PaymentFailureScenario determineFailureScenario() {
        int scenario = random.nextInt(10);

        return switch (scenario) {
            case 0, 1, 2 -> new PaymentFailureScenario(
                "Insufficient funds in account",
                "INSUFFICIENT_FUNDS"
            );
            case 3, 4 -> new PaymentFailureScenario(
                "Transaction declined by bank",
                "TRANSACTION_DECLINED"
            );
            case 5 -> new PaymentFailureScenario(
                "Card expired or invalid",
                "CARD_INVALID"
            );
            case 6 -> new PaymentFailureScenario(
                "Payment gateway timeout",
                "GATEWAY_TIMEOUT"
            );
            case 7 -> new PaymentFailureScenario(
                "Transaction limit exceeded",
                "LIMIT_EXCEEDED"
            );
            default -> new PaymentFailureScenario(
                "Payment processing error",
                "PROCESSING_ERROR"
            );
        };
    }

    /**
     * Retry payment thất bại
     *
     * @param paymentId Payment ID
     * @return Payment sau khi retry
     */
    @Transactional
    public Payment retryPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (!payment.canRetry()) {
            throw new IllegalStateException(
                "Payment cannot be retried. Status: " + payment.paymentStatus +
                ", Retries: " + payment.retryCount + "/" + payment.maxRetryAttempts
            );
        }

        log.info("Retrying payment {} (attempt {}/{})",
            payment.paymentNumber, payment.retryCount + 1, payment.maxRetryAttempts);

        payment.retryCount++;
        payment.paymentStatus = com.payment.domain.enums.PaymentStatus.PROCESSING;
        payment.processingStartedAt = LocalDateTime.now();
        payment.failureReason = null;
        payment.errorCode = null;

        return processPayment(payment);
    }

    /**
     * Inner class representing payment failure scenario
     */
    private static class PaymentFailureScenario {
        private final String reason;
        private final String errorCode;

        public PaymentFailureScenario(String reason, String errorCode) {
            this.reason = reason;
            this.errorCode = errorCode;
        }

        public String getReason() {
            return reason;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
