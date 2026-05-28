package com.payment.domain.repository;

import com.payment.domain.entity.VnpIpnLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho VnpIpnLog entity.
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Repository
public interface VnpIpnLogRepository extends JpaRepository<VnpIpnLog, java.util.UUID> {

    /**
     * Tìm IPN log theo VNPAY transaction no.
     *
     * @param vnpTransactionNo Mã giao dịch VNPAY
     * @return Optional chứa IPN log nếu tìm thấy
     */
    Optional<VnpIpnLog> findByVnpTransactionNo(String vnpTransactionNo);

    /**
     * Tìm IPN logs theo payment transaction log ID.
     *
     * @param paymentTransactionLogId ID payment transaction log
     * @return Danh sách IPN logs
     */
    List<VnpIpnLog> findByPaymentTransactionLogId(java.util.UUID paymentTransactionLogId);

    /**
     * Tìm IPN logs thành công trong khoảng thời gian.
     *
     * @param from Thời gian bắt đầu
     * @param to Thời gian kết thúc
     * @return Danh sách IPN logs thành công
     */
    @Query("SELECT i FROM VnpIpnLog i WHERE i.vnpResponseCode = '00' AND i.createdAt BETWEEN :from AND :to ORDER BY i.createdAt DESC")
    List<VnpIpnLog> findSuccessfulIpnLogsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Tìm IPN logs thất bại trong khoảng thời gian.
     *
     * @param from Thời gian bắt đầu
     * @param to Thời gian kết thúc
     * @return Danh sách IPN logs thất bại
     */
    @Query("SELECT i FROM VnpIpnLog i WHERE i.vnpResponseCode != '00' AND i.createdAt BETWEEN :from AND :to ORDER BY i.createdAt DESC")
    List<VnpIpnLog> findFailedIpnLogsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Kiểm tra IPN log đã tồn tại theo VNPAY transaction no.
     *
     * @param vnpTransactionNo Mã giao dịch VNPAY
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByVnpTransactionNo(String vnpTransactionNo);

    /**
     * Đếm số IPN logs theo payment transaction log ID.
     *
     * @param paymentTransactionLogId ID payment transaction log
     * @return Số lượng IPN logs
     */
    long countByPaymentTransactionLogId(java.util.UUID paymentTransactionLogId);

    /**
     * Tìm IPN logs gần đây theo thời gian tạo.
     *
     * @param from Thời gian bắt đầu
     * @param to Thời gian kết thúc
     * @return Danh sách IPN logs
     */
    @Query("SELECT i FROM VnpIpnLog i WHERE i.createdAt BETWEEN :from AND :to ORDER BY i.createdAt DESC")
    List<VnpIpnLog> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
