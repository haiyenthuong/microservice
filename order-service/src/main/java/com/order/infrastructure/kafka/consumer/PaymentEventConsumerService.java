package com.order.infrastructure.kafka.consumer;

import com.order.domain.model.Order;
import com.order.domain.repository.OrderRepository;
import com.order.infrastructure.kafka.event.OrderPaidEvent;
import com.order.infrastructure.kafka.event.OrderPaymentFailedEvent;
import com.order.infrastructure.kafka.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service xử lý business logic cho Payment Events
 *
 * Service này thực hiện các business operations khi nhận Payment events:
 * - Tìm Order trong DB
 * - Update Order status (PAID hoặc FAILED)
 * - Lưu thông tin transaction
 * - Publish các event tiếp theo (OrderPaidEvent hoặc OrderPaymentFailedEvent)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumerService {

    private final OrderRepository orderRepository;
    private final OrderPaymentResultEventPublisher eventPublisher;

    /**
     * Xử lý PaymentProcessedEvent thành công
     *
     * Flow:
     * 1. Find Order by ID
     * 2. Check idempotency (đã PAID chưa?)
     * 3. Update Order status → PAID
     * 4. Save transaction info
     * 5. Publish OrderPaidEvent
     *
     * @param event PaymentProcessedEvent với success = true
     */
    @Transactional
    public void handlePaymentSuccess(PaymentProcessedEvent event) {
        log.info("Processing PaymentSuccess for order: {} | Payment ID: {} | Transaction ID: {}",
                event.getOrderId(), event.getPaymentId(), event.getTransactionId());

        // Step 1: Find Order by ID
        Order order = findOrderById(event.getOrderId());

        // Step 2: Check idempotency - nếu đã PAID thì skip
        if (order.getPaymentStatus().isPaid()) {
            log.warn("Order {} is already PAID. Skipping duplicate PaymentSuccess event. " +
                    "Current Transaction ID: {}, New Transaction ID: {}",
                    order.getOrderCode(), order.getTransactionId(), event.getTransactionId());
            return;
        }

        if (order.getPaymentStatus().isFailed()) {
            log.info("Order {} was FAILED, now retrying payment. Updating to PAID. " +
                    "Previous failure reason: {}", order.getOrderCode(), order.getPaymentFailureReason());
            // Clear previous failure reason
            order.setPaymentFailureReason(null);
        }

        // Step 3: Update Order status → PAID
        log.info("Marking order {} as PAID with transaction ID: {}",
                order.getOrderCode(), event.getTransactionId());
        order.markAsPaid(event.getTransactionId());

        // Additional info
        if (event.getPaymentMethod() != null) {
            order.setPaymentMethod(event.getPaymentMethod());
        }

        // Step 4: Save Order
        Order savedOrder = orderRepository.save(order);
        log.info("Successfully updated order {} to PAID status. Transaction ID: {}",
                savedOrder.getOrderCode(), savedOrder.getTransactionId());

        // Step 5: Publish OrderPaidEvent
        publishOrderPaidEvent(savedOrder, event);

        log.info("Completed PaymentSuccess processing for order: {}", event.getOrderId());
    }

    /**
     * Xử lý PaymentProcessedEvent thất bại
     *
     * Flow:
     * 1. Find Order by ID
     * 2. Check idempotency (đã FAILED chưa?)
     * 3. Update Order status → FAILED
     * 4. Save failure reason và error code
     * 5. Publish OrderPaymentFailedEvent
     *
     * @param event PaymentProcessedEvent với success = false
     */
    @Transactional
    public void handlePaymentFailed(PaymentProcessedEvent event) {
        log.info("Processing PaymentFailed for order: {} | Payment ID: {} | Reason: {} | Retryable: {}",
                event.getOrderId(), event.getPaymentId(), event.getFailureReason(), event.isRetryable());

        // Step 1: Find Order by ID
        Order order = findOrderById(event.getOrderId());

        // Step 2: Check idempotency - nếu đã FAILED thì chỉ update nếu có info mới
        if (order.getPaymentStatus().isFailed()) {
            log.warn("Order {} is already FAILED. Current reason: '{}', New reason: '{}'. " +
                    "Retryable: {} | Retry count: {}",
                    order.getOrderCode(), order.getPaymentFailureReason(), event.getFailureReason(),
                    event.isRetryable(), getRetryCount(order));
            // Có thể update retry count và reason nếu có
            // nhưng không publish event lại để tránh duplicate
            return;
        }

        // Build failure reason with error code
        String failureReason = buildFailureReason(event);

        // Step 3: Update Order status → FAILED
        log.info("Marking order {} as FAILED. Reason: {}", order.getOrderCode(), failureReason);
        order.markPaymentFailed(failureReason);

        // Step 4: Save Order
        Order savedOrder = orderRepository.save(order);
        log.info("Successfully updated order {} to FAILED status. Reason: {}",
                savedOrder.getOrderCode(), savedOrder.getPaymentFailureReason());

        // Step 5: Publish OrderPaymentFailedEvent
        publishOrderPaymentFailedEvent(savedOrder, event);

        log.info("Completed PaymentFailed processing for order: {}", event.getOrderId());
    }

    /**
     * Tìm Order theo ID
     *
     * @param orderId Order ID
     * @return Order entity
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    private Order findOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new IllegalArgumentException("Order not found: " + orderId);
                });
    }

    /**
     * Build failure reason message chi tiết
     *
     * @param event PaymentProcessedEvent
     * @return Failure reason string
     */
    private String buildFailureReason(PaymentProcessedEvent event) {
        StringBuilder reason = new StringBuilder();

        if (event.getFailureReason() != null) {
            reason.append(event.getFailureReason());
        }

        if (event.getErrorCode() != null) {
            reason.append(" (Error Code: ").append(event.getErrorCode()).append(")");
        }

        if (event.isRetryable()) {
            reason.append(" - Retryable");
        }

        return reason.length() > 0 ? reason.toString() : "Payment processing failed";
    }

    /**
     * Lấy retry count từ Order (dựa trên số lần failed liên tiếp)
     *
     * @param order Order entity
     * @return Retry count
     */
    private int getRetryCount(Order order) {
        // TODO: Implement retry count tracking
        // Có thể thêm field retryCount vào Order entity
        return 0;
    }

    /**
     * Publish OrderPaidEvent sau khi xử lý thành công
     *
     * @param order        Order đã được update
     * @param paymentEvent PaymentProcessedEvent gốc
     */
    private void publishOrderPaidEvent(Order order, PaymentProcessedEvent paymentEvent) {
        try {
            OrderPaidEvent event = new OrderPaidEvent(
                    UUID.randomUUID().toString(), // eventId
                    order.getId(), // aggregateId
                    Instant.now(), // timestamp
                    order.getUserId(), // userId
                    paymentEvent.getTraceId(), // traceId
                    order.getId(), // orderId
                    order.getOrderCode(), // orderNumber
                    paymentEvent.getPaymentId(), // paymentId
                    order.getTransactionId(), // transactionId
                    order.getAmount(), // amount
                    order.getCurrency(), // currency
                    order.getPaymentMethod() // paymentMethod
            );

            eventPublisher.publishOrderPaidEvent(event);
            log.info("Published OrderPaidEvent for order: {}", order.getOrderCode());

        } catch (Exception e) {
            log.error("Failed to publish OrderPaidEvent for order {}: {}",
                    order.getOrderCode(), e.getMessage(), e);
            // Không throw exception vì Order đã được save thành công
        }
    }

    /**
     * Publish OrderPaymentFailedEvent sau khi xử lý thất bại
     *
     * @param order        Order đã được update
     * @param paymentEvent PaymentProcessedEvent gốc
     */
    private void publishOrderPaymentFailedEvent(Order order, PaymentProcessedEvent paymentEvent) {
        try {
            OrderPaymentFailedEvent event = new OrderPaymentFailedEvent(
                    UUID.randomUUID().toString(), // eventId
                    order.getId(), // aggregateId
                    Instant.now(), // timestamp
                    order.getUserId(), // userId
                    paymentEvent.getTraceId(), // traceId
                    order.getId(), // orderId
                    order.getOrderCode(), // orderNumber
                    order.getAmount(), // amount
                    order.getCurrency(), // currency
                    order.getPaymentFailureReason(), // reason
                    paymentEvent.getErrorCode(), // errorCode
                    paymentEvent.isRetryable() // retryable
            );

            eventPublisher.publishOrderPaymentFailedEvent(event);
            log.info("Published OrderPaymentFailedEvent for order: {}", order.getOrderCode());

        } catch (Exception e) {
            log.error("Failed to publish OrderPaymentFailedEvent for order {}: {}",
                    order.getOrderCode(), e.getMessage(), e);
            // Không throw exception vì Order đã được save thành công
        }
    }
}
