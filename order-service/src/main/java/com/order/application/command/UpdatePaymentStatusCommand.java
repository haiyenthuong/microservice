package com.order.application.command;

import com.order.domain.common.ResourceNotFoundException;
import com.order.domain.model.Order;
import com.order.domain.model.PaymentStatus;
import com.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command để cập nhật trạng thái thanh toán cho order.
 * Dùng khi nhận callback từ payment-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePaymentStatusCommand implements ICommand {

    private final OrderRepository orderRepository;

    /**
     * Execute command để cập nhật trạng thái thanh toán.
     *
     * @param orderId ID của đơn hàng
     * @param paymentStatus trạng thái thanh toán (PAID, FAILED)
     * @param transactionId ID giao dịch (nếu có)
     * @param failureReason lý do thất bại (nếu có)
     */
    @Transactional
    public void execute(String orderId, String paymentStatus, String transactionId, String failureReason) {
        log.info("Updating payment status for order: {} to {}", orderId, paymentStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if ("PAID".equalsIgnoreCase(paymentStatus)) {
            order.markAsPaid(transactionId);
            log.info("Order {} marked as PAID with transaction ID: {}", orderId, transactionId);
        } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
            order.markPaymentFailed(failureReason);
            log.warn("Order {} marked as FAILED. Reason: {}", orderId, failureReason);
        }

        orderRepository.save(order);
    }
}
