package com.payment.domain.repository;

import com.payment.domain.entity.Payment;
import com.payment.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface cho Payment entity
 *
 * Cung cấp các methods để query và manipulate Payment entities.
 * Payment Service sử dụng repository này để lưu trữ và truy xuất
 * thông tin giao dịch thanh toán.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Tìm payment theo order ID
     *
     * @param orderId Order ID
     * @return Optional chứa payment nếu tìm thấy
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Tìm payments theo user ID
     *
     * @param userId User ID
     * @return List của payments của user
     */
    List<Payment> findByUserId(String userId);

    /**
     * Tìm payment theo transaction ID
     *
     * @param transactionId Transaction ID từ payment gateway
     * @return Optional chứa payment nếu tìm thấy
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Tìm payment theo payment number
     *
     * @param paymentNumber Số payment
     * @return Optional chứa payment nếu tìm thấy
     */
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    /**
     * Tìm payments theo trạng thái thanh toán
     *
     * @param status Payment Status enum
     * @return List của payments với trạng thái đã cho
     */
    List<Payment> findByPaymentStatus(PaymentStatus status);

    /**
     * Tìm payments theo payment method
     *
     * @param paymentMethod Payment Method enum
     * @return List của payments với phương thức đã cho
     */
    List<Payment> findByPaymentMethod(com.payment.domain.enums.PaymentMethod paymentMethod);

    /**
     * Tìm payments cần retry
     *
     * Điều kiện:
     * - Status = FAILED
     * - retryCount < maxRetryAttempts
     * - nextRetryAt <= now (hoặc null)
     *
     * @return List của payments cần retry
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'FAILED' " +
           "AND (p.retryCount < p.maxRetryAttempts OR p.maxRetryAttempts IS NULL) " +
           "AND (p.nextRetryAt IS NULL OR p.nextRetryAt <= :now)")
    List<Payment> findPaymentsNeedRetry(@Param("now") LocalDateTime now);

    /**
     * Tìm payments đang processing quá lâu
     *
     * @param threshold Ngưỡng thời gian (ví dụ: 5 phút trước)
     * @return List của payments đang processing quá lâu
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PROCESSING' " +
           "AND p.processingStartedAt < :threshold")
    List<Payment> findStuckProcessingPayments(@Param("threshold") LocalDateTime threshold);

    /**
     * Tìm payments đã được tạo trong khoảng thời gian
     *
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @return List của payments trong khoảng thời gian
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    List<Payment> findPaymentsCreatedBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Đếm số payments theo order ID
     *
     * @param orderId Order ID
     * @return Số payments cho order này
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.orderId = :orderId")
    long countByOrderId(@Param("orderId") String orderId);

    /**
     * Lấy payment cuối cùng theo order ID
     *
     * @param orderId Order ID
     * @return Optional chứa payment mới nhất
     */
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC")
    Optional<Payment> findLatestByOrderId(@Param("orderId") String orderId);

    /**
     * Kiểm tra xem order có payment đang processing không
     *
     * @param orderId Order ID
     * @return true nếu có payment đang processing
     */
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = 'PROCESSING'")
    boolean existsProcessingPaymentForOrder(@Param("orderId") String orderId);

    /**
     * Tìm tất cả payments cần được retry ngay bây giờ
     *
     * @param now Current time
     * @return List của payments có thể retry
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'FAILED' " +
           "AND (p.retryCount < p.maxRetryAttempts OR p.maxRetryAttempts IS NULL) " +
           "AND (p.nextRetryAt IS NULL OR p.nextRetryAt <= :now)")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    List<Payment> findRetryablePaymentsNow(@Param("now") LocalDateTime now);

    /**
     * Lấy tổng số tiền thanh toán thành công theo khoảng thời gian
     *
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @return Tổng số tiền
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'PAID' " +
           "AND p.paidAt BETWEEN :startTime AND :endTime")
    java.math.BigDecimal getTotalPaidAmountBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
