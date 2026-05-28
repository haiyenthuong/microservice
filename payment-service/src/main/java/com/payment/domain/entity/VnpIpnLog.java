package com.payment.domain.entity;

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
 * VNPAY IPN Log Entity
 *
 * Entity lưu trữ thông tin IPN (Instant Payment Notification) từ VNPAY
 * Dùng để log và verify các callback từ VNPAY
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
@Table(name = "vnp_ipn_logs")
public class VnpIpnLog extends BaseEntity {

    /**
     * ID của payment transaction log liên quan
     */
    @Column(name = "payment_transaction_log_id", length = 36)
    private String paymentTransactionLogId;

    /**
     * Mã giao dịch VNPAY
     */
    @Column(name = "vnp_transaction_no", length = 50)
    private String vnpTransactionNo;

    /**
     * Mã ngân hàng
     */
    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode;

    /**
     * Mã giao dịch tại ngân hàng
     */
    @Column(name = "vnp_bank_tran_no", length = 50)
    private String vnpBankTranNo;

    /**
     * Loại thẻ
     */
    @Column(name = "vnp_card_type", length = 20)
    private String vnpCardType;

    /**
     * Số tiền giao dịch (VND)
     */
    @Column(name = "vnp_amount", precision = 19, scale = 2)
    private BigDecimal vnpAmount;

    /**
     * Mã phản hồi
     */
    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    /**
     * Mã trạng thái giao dịch
     */
    @Column(name = "vnp_transaction_status", length = 10)
    private String vnpTransactionStatus;

    /**
     * Thông tin đơn hàng
     */
    @Column(name = "vnp_order_info", length = 500)
    private String vnpOrderInfo;

    /**
     * Thời gian thanh toán (format: yyyymmddHHmmss)
     */
    @Column(name = "vnp_pay_date", length = 14)
    private String vnpPayDate;

    /**
     * Mã terminal VNPAY
     */
    @Column(name = "vnp_tmn_code", length = 20)
    private String vnpTmnCode;

    /**
     * Chuỗi checksum để verify integrity của data
     */
    @Column(name = "vnp_secure_hash", length = 255)
    private String vnpSecureHash;

    /**
     * Checksum có hợp lệ không
     */
    @Column(name = "is_valid_checksum", nullable = false)
    @Builder.Default
    private Boolean isValidChecksum = false;

    /**
     * Response code từ hệ thống (xử lý IPN)
     */
    @Column(name = "response_code", length = 10)
    private String responseCode;

    /**
     * Response message từ hệ thống
     */
    @Column(name = "response_message", length = 500)
    private String responseMessage;

    /**
     * Raw data từ VNPAY (JSON format)
     */
    @Lob
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    /**
     * Thời gian tạo record (created_at)
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Version number cho row-level locking
     */
    @Column(name = "row_version", nullable = false)
    @Builder.Default
    private Integer rowVersion = 1;

    /**
     * Kiểm tra IPN thành công (response code = "00")
     */
    public boolean isSuccessful() {
        return "00".equals(vnpResponseCode);
    }

    /**
     * Kiểm tra checksum hợp lệ
     */
    public boolean hasValidChecksum() {
        return isValidChecksum != null && isValidChecksum;
    }

    /**
     * Đánh dấu IPN đã được verify thành công
     */
    public void markAsVerified() {
        this.isValidChecksum = true;
        this.responseCode = "00";
        this.responseMessage = "IPN verified successfully";
    }

    /**
     * Đánh dấu IPN verify thất bại
     */
    public void markAsVerificationFailed(String reason) {
        this.isValidChecksum = false;
        this.responseCode = "01";
        this.responseMessage = reason;
    }

    /**
     * Lấy thời gian pay date dưới dạng LocalDateTime
     * Parse từ format yyyymmddHHmmss
     */
    public LocalDateTime getPayDateAsDateTime() {
        if (vnpPayDate == null || vnpPayDate.length() != 14) {
            return null;
        }
        try {
            int year = Integer.parseInt(vnpPayDate.substring(0, 4));
            int month = Integer.parseInt(vnpPayDate.substring(4, 6));
            int day = Integer.parseInt(vnpPayDate.substring(6, 8));
            int hour = Integer.parseInt(vnpPayDate.substring(8, 10));
            int minute = Integer.parseInt(vnpPayDate.substring(10, 12));
            int second = Integer.parseInt(vnpPayDate.substring(12, 14));
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Callback trước khi lưu
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isValidChecksum == null) {
            isValidChecksum = false;
        }
        if (rowVersion == null) {
            rowVersion = 1;
        }
    }
}
