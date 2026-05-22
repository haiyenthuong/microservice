package com.payment.domain.entity;

import com.payment.domain.enums.PaymentMethod;
import com.payment.domain.enums.PaymentProvider;
import com.payment.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Entity - Lưu trữ thông tin giao dịch thanh toán
 *
 * Entity này chứa đầy đủ thông tin về một giao dịch thanh toán,
 * bao gồm thông tin đơn hàng, phương thức, trạng thái, và kết quả xử lý.
 *
 * Key Features:
 * - Mỗi Payment liên kết với 1 Order (orderId)
 * - Lưu vết lịch sử xử lý (PROCESSING → PAID/FAILED)
 * - Hỗ trợ multiple payment providers (Stripe, PayPal, VNPAY, etc.)
 * - Transaction ID để track với payment gateway
 * - Failure reason để debug khi thanh toán thất bại
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_status", columnList = "payment_status"),
    @Index(name = "idx_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Payment extends BaseEntity {

    /**
     * Order ID - Liên kết với Order trong Order Service
     * Đây là correlation ID để track payment flow
     */
    @Column(name = "order_id", length = 36, nullable = false)
    public String orderId;

    /**
     * Payment Number - Số tham chiếu của payment (unique)
     * Format: PAY-YYYYMMDDHHMMSS-XXXXXXXX
     */
    @Column(name = "payment_number", length = 50, nullable = false, unique = true)
    public String paymentNumber;

    /**
     * User ID - ID của user thực hiện thanh toán
     */
    @Column(name = "user_id", length = 36, nullable = false)
    public String userId;

    /**
     * Username - Tên đăng nhập của user
     */
    @Column(name = "username", length = 100)
    public String username;

    /**
     * Customer Name - Tên đầy đủ của customer
     */
    @Column(name = "customer_name", length = 200)
    public String customerName;

    /**
     * Customer Email - Email của customer
     */
    @Column(name = "customer_email", length = 100)
    public String customerEmail;

    /**
     * Customer Phone - Số điện thoại của customer
     */
    @Column(name = "customer_phone", length = 20)
    public String customerPhone;

    /**
     * Payment Method - Phương thức thanh toán (CREDIT_CARD, VNPAY, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    public PaymentMethod paymentMethod;

    /**
     * Payment Provider - Nhà cung cấp cổng thanh toán (STRIPE, VNPAY, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", length = 50)
    public PaymentProvider paymentProvider;

    /**
     * Payment Status - Trạng thái thanh toán
     * PROCESSING → PAID hoặc FAILED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    public PaymentStatus paymentStatus;

    /**
     * Số tiền cần thanh toán
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    /**
     * Currency code (USD, VND, EUR, etc.)
     */
    @Column(name = "currency", length = 3, nullable = false)
    public String currency;

    /**
     * Transaction ID - ID giao dịch từ payment gateway
     * Đây là ID trả về từ Stripe, PayPal, VNPAY, etc.
     */
    @Column(name = "transaction_id", length = 100)
    public String transactionId;

    /**
     * Gateway Transaction ID - Internal transaction ID của payment gateway
     * Một số provider có transaction ID riêng biệt
     */
    @Column(name = "gateway_transaction_id", length = 100)
    public String gatewayTransactionId;

    /**
     * Payment Gateway Response - JSON response từ payment gateway
     * Lưu để debug và reconcile
     */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    public String gatewayResponse;

    /**
     * Failure Reason - Lý do thất bại (nếu có)
     */
    @Column(name = "failure_reason", length = 1000)
    public String failureReason;

    /**
     * Error Code - Mã lỗi từ payment gateway
     */
    @Column(name = "error_code", length = 50)
    public String errorCode;

    /**
     * Metadata - Thông tin bổ sung dưới dạng JSON
     * Chứa các thông tin custom như: IP address, device info, etc.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    public String metadata;

    /**
     * Client IP - IP address của client khi thực hiện payment
     */
    @Column(name = "client_ip", length = 50)
    public String clientIp;

    /**
     * User Agent - User agent string của browser/client
     */
    @Column(name = "user_agent", length = 500)
    public String userAgent;

    /**
     * Processing Started At - Thời điểm bắt đầu xử lý
     */
    @Column(name = "processing_started_at")
    public LocalDateTime processingStartedAt;

    /**
     * Processing Completed At - Thời điểm hoàn thành xử lý
     */
    @Column(name = "processing_completed_at")
    public LocalDateTime processingCompletedAt;

    /**
     * Paid At - Thời điểm thanh toán thành công
     */
    @Column(name = "paid_at")
    public LocalDateTime paidAt;

    /**
     * Retry Count - Số lần đã retry (cho failed payments)
     */
    @Column(name = "retry_count", nullable = false)
    public Integer retryCount = 0;

    /**
     * Max Retry Attempts - Số lần retry tối đa cho phép
     */
    @Column(name = "max_retry_attempts", nullable = false)
    public Integer maxRetryAttempts = 3;

    /**
     * Next Retry At - Thời điểm retry tiếp theo
     */
    @Column(name = "next_retry_at")
    public LocalDateTime nextRetryAt;

    /**
     * Lấy Payment Status dưới dạng enum
     */
    public PaymentStatus getStatus() {
        return paymentStatus;
    }

    /**
     * Thiết lập Payment Status từ enum
     */
    public void setStatus(PaymentStatus status) {
        this.paymentStatus = status;
    }

    /**
     * Kiểm tra payment có đang ở trạng thái PROCESSING không
     */
    public boolean isProcessing() {
        return paymentStatus == PaymentStatus.PROCESSING;
    }

    /**
     * Kiểm tra payment có thành công không
     */
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    /**
     * Kiểm tra payment có thất bại không
     */
    public boolean isFailed() {
        return paymentStatus == PaymentStatus.FAILED;
    }

    /**
     * Đánh dấu payment đang được xử lý
     */
    public void markAsProcessing() {
        this.paymentStatus = PaymentStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }

    /**
     * Đánh dấu payment thành công
     *
     * @param transactionId Transaction ID từ payment gateway
     * @param gatewayTransactionId Optional gateway transaction ID
     * @param gatewayResponse Optional gateway response
     */
    public void markAsPaid(String transactionId, String gatewayTransactionId, String gatewayResponse) {
        this.paymentStatus = PaymentStatus.PAID;
        this.transactionId = transactionId;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponse = gatewayResponse;
        this.paidAt = LocalDateTime.now();
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Đánh dấu payment thất bại
     *
     * @param failureReason Lý do thất bại
     * @param errorCode Mã lỗi từ payment gateway
     */
    public void markAsFailed(String failureReason, String errorCode) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.errorCode = errorCode;
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Đánh dấu payment bị hủy
     */
    public void markAsCancelled() {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Tăng retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * Kiểm tra có thể retry payment không
     * Chỉ retry khi failed và chưa vượt quá max retry attempts
     */
    public boolean canRetry() {
        return paymentStatus == PaymentStatus.FAILED &&
                (retryCount == null || retryCount < maxRetryAttempts);
    }

    /**
     * Thiết lập thời điểm retry tiếp theo
     *
     * @param nextRetryAt Thời điểm retry tiếp theo
     */
    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    /**
     * Tạo payment number unique
     */
    public String generatePaymentNumber() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + timestamp + "-" + uniqueId;
    }

    /**
     * Callback trước khi lưu - tự động tạo payment number
     */
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (paymentNumber == null || paymentNumber.isEmpty()) {
            paymentNumber = generatePaymentNumber();
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PROCESSING;
        }
    }
}
