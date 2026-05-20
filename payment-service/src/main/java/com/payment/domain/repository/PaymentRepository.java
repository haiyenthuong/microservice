package com.payment.domain.repository;

import com.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Tìm payment theo order ID.
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Tìm tất cả payments theo user ID.
     */
    List<Payment> findByUserId(String userId);

    /**
     * Tìm payment theo transaction ID.
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Tìm payments theo trạng thái thanh toán.
     */
    List<Payment> findByPaymentStatus(Integer paymentStatus);
}
