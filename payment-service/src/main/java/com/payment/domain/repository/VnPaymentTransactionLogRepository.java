package com.payment.domain.repository;

import com.payment.domain.entity.VnPaymentTransactionLog;
import com.payment.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho VnPaymentTransactionLog entity.
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Repository
public interface VnPaymentTransactionLogRepository extends JpaRepository<VnPaymentTransactionLog, java.util.UUID> {

    /**
     * Tìm transaction log theo order code.
     *
     * @param orderCode Mã đơn hàng
     * @return Optional chứa transaction log nếu tìm thấy
     */
    Optional<VnPaymentTransactionLog> findByOrderCode(String orderCode);

    /**
     * Kiểm tra order code tồn tại.
     *
     * @param orderCode Mã đơn hàng
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByOrderCode(String orderCode);

    /**
     * Tìm tất cả transactions theo order ID.
     *
     * @param ordersId ID đơn hàng
     * @return Danh sách transactions
     */
    List<VnPaymentTransactionLog> findByOrdersId(java.util.UUID ordersId);

    /**
     * Tìm tất cả transactions theo user ID.
     *
     * @param userId ID người dùng
     * @return Danh sách transactions
     */
    List<VnPaymentTransactionLog> findByUserId(java.util.UUID userId);

    /**
     * Tìm transactions theo loại và trạng thái.
     *
     * @param transactionType Loại giao dịch
     * @param status Trạng thái
     * @return Danh sách transactions
     */
    @Query("SELECT t FROM VnPaymentTransactionLog t WHERE t.transactionType = :transactionType AND t.status = :status")
    List<VnPaymentTransactionLog> findByTransactionTypeAndStatus(
            @Param("transactionType") TransactionType transactionType,
            @Param("status") String status
    );

    /**
     * Tìm transactions hết hạn.
     *
     * @param currentDateTime Thời gian hiện tại
     * @return Danh sách transactions đã hết hạn
     */
    @Query("SELECT t FROM VnPaymentTransactionLog t WHERE t.expiredAt < :currentDateTime AND t.status = 'PENDING'")
    List<VnPaymentTransactionLog> findExpiredTransactions(@Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Tìm transactions theo parent transaction ID (cho refund).
     *
     * @param parentTransactionId ID giao dịch gốc
     * @return Danh sách transactions hoàn tiền
     */
    List<VnPaymentTransactionLog> findByParentTransactionId(java.util.UUID parentTransactionId);

    /**
     * Đếm transactions theo order ID và loại.
     *
     * @param ordersId ID đơn hàng
     * @param transactionType Loại giao dịch
     * @return Số lượng transactions
     */
    long countByOrdersIdAndTransactionType(java.util.UUID ordersId, TransactionType transactionType);

    /**
     * Tìm transactions gần đây theo thời gian tạo.
     *
     * @param from Thời gian bắt đầu
     * @param to Thời gian kết thúc
     * @return Danh sách transactions
     */
    @Query("SELECT t FROM VnPaymentTransactionLog t WHERE t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<VnPaymentTransactionLog> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
