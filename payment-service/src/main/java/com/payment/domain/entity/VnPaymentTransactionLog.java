package com.payment.domain.entity;

import com.payment.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * VNPAY Payment Transaction Log Entity
 *
 * Entity lưu trữ thông tin giao dịch thanh toán qua VNPAY
 * Bao gồm cả thanh toán (PAYMENT) và hoàn tiền (REFUND)
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "vn_payment_transaction_log", indexes = {
    @Index(name = "IX_vn_payment_transaction_log_parent_transaction_id", columnList = "parent_transaction_id"),
    @Index(name = "IX_vn_payment_transaction_log_sale_transaction_id", columnList = "orders_id"),
    @Index(name = "IX_vn_payment_transaction_log_status_transaction_type", columnList = "status, transaction_type")
})
public class VnPaymentTransactionLog extends BaseEntity {

    /**
     * Loại giao dịch: PAYMENT (thanh toán) hoặc REFUND (hoàn tiền)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    /**
     * Mã đơn thanh toán, map với vnp_TxnRef. Unique trong ngày
     */
    @Column(name = "order_code", length = 100, nullable = false, unique = true)
    private String orderCode;

    /**
     * ID giao dịch bán hàng (FK → Orders.Id)
     */
    @Column(name = "orders_id", length = 36, nullable = false)
    private String ordersId;

    /**
     * ID người tạo yêu cầu thanh toán (FK → AdmUsers.Id)
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * Số tiền thanh toán/hoàn tiền (VND)
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Số tiền gửi VNPAY (= Amount × 100)
     */
    @Column(name = "vnp_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal vnpAmount;

    /**
     * Trạng thái giao dịch
     * PAYMENT: PENDING/PAID/FAILED/EXPIRED
     * REFUND: PENDING/SUCCESS/FAILED
     */
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    /**
     * Phương thức thanh toán
     */
    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    /**
     * Mã ngân hàng (khi thanh toán thành công)
     */
    @Column(name = "bank_code", length = 50)
    private String bankCode;

    /**
     * Mã terminal VNPAY
     */
    @Column(name = "vnp_tmn_code", length = 50, nullable = false)
    private String vnpTmnCode;

    /**
     * URL trả về sau khi thanh toán
     */
    @Column(name = "return_url", length = 1000, nullable = false)
    private String returnUrl;

    /**
     * URL thanh toán (được generate từ VNPAY)
     */
    @Column(name = "payment_url", length = 2000)
    private String paymentUrl;

    /**
     * IP address của client
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * Ngôn ngữ (vn, en)
     */
    @Column(name = "locale", length = 10)
    private String locale;

    /**
     * Thời gian hết hạn giao dịch
     */
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    /**
     * Thời gian thanh toán thành công
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Mô tả giao dịch
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * ID giao dịch thanh toán gốc (chỉ dùng khi TransactionType = REFUND)
     * FK → self. ID GD thanh toán gốc
     */
    @Column(name = "parent_transaction_id", length = 36)
    private String parentTransactionId;

    /**
     * Lý do hoàn tiền (chỉ dùng khi TransactionType = REFUND)
     */
    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    /**
     * JSON chứa toàn bộ params request gửi sang VNPAY khi tạo URL thanh toán
     */
    @Lob
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    /**
     * JSON chứa toàn bộ params response nhận từ VNPAY qua Return URL
     */
    @Lob
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    /**
     * Kiểm tra có phải giao dịch thanh toán không.
     */
    public boolean isPayment() {
        return transactionType != null && transactionType.isPayment();
    }

    /**
     * Kiểm tra có phải giao dịch hoàn tiền không.
     */
    public boolean isRefund() {
        return transactionType != null && transactionType.isRefund();
    }

    /**
     * Kiểm tra giao dịch đang chờ xử lý.
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    /**
     * Kiểm tra giao dịch thành công.
     */
    public boolean isSuccessful() {
        return "PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status);
    }

    /**
     * Kiểm tra giao dịch thất bại.
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status);
    }

    /**
     * Kiểm tra giao dịch đã hết hạn.
     */
    public boolean isExpired() {
        return "EXPIRED".equalsIgnoreCase(status);
    }

    /**
     * Đánh dấu giao dịch đang chờ xử lý.
     */
    public void markAsPending() {
        this.status = "PENDING";
    }

    /**
     * Đánh dấu giao dịch thanh toán thành công.
     *
     * @param bankCode Mã ngân hàng
     * @param vnpTransactionNo Mã giao dịch VNPAY
     */
    public void markAsPaid(String bankCode, String vnpTransactionNo) {
        this.status = "PAID";
        this.bankCode = bankCode;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Đánh dấu hoàn tiền thành công.
     */
    public void markRefundSuccess() {
        this.status = "SUCCESS";
    }

    /**
     * Đánh dấu giao dịch thất bại.
     *
     * @param failureReason Lý do thất bại
     */
    public void markAsFailed(String failureReason) {
        this.status = "FAILED";
        this.description = failureReason;
    }

    /**
     * Đánh dấu giao dịch hết hạn.
     */
    public void markAsExpired() {
        this.status = "EXPIRED";
    }

    /**
     * Generate order code unique theo format VNPAY
     * Format: YYYYMMDD + random string (đảm bảo unique trong ngày)
     */
    public String generateOrderCode() {
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return datePart + randomPart;
    }

    /**
     * Callback trước khi lưu - tự động tạo order code
     */
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (orderCode == null || orderCode.isEmpty()) {
            orderCode = generateOrderCode();
        }
        if (status == null || status.isEmpty()) {
            status = "PENDING";
        }
        // Set expired_at to 15 minutes from now if not set
        if (expiredAt == null && isPayment()) {
            expiredAt = LocalDateTime.now().plusMinutes(15);
        }
    }
}
