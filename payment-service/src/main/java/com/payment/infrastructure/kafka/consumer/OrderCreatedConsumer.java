package com.payment.infrastructure.kafka.consumer;

import com.payment.application.service.PaymentService;
import com.payment.domain.entity.Payment;
import com.payment.domain.enums.PaymentMethod;
import com.payment.domain.enums.PaymentProvider;
import com.payment.domain.enums.PaymentStatus;
import com.payment.domain.repository.PaymentRepository;
import com.payment.infrastructure.kafka.event.OrderCreatedEvent;
import com.payment.infrastructure.kafka.event.PaymentProcessedEvent;
import com.payment.infrastructure.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Consumer xử lý OrderCreatedEvent từ Kafka
 *
 * Nhận sự kiện đơn hàng mới được tạo, thực hiện xử lý thanh toán
 * và publish kết quả thanh toán lại Kafka.
 */
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final PaymentService paymentService;

    /**
     * Lắng nghe và xử lý OrderCreatedEvent từ Kafka topic "order-events"
     *
     * Sử dụng batch listener để xử lý nhiều event cùng lúc để tối ưu performance.
     * Mỗi event được xử lý độc lập với transaction riêng.
     *
     * @param events Danh sách OrderCreatedEvent
     * @param partitions Partition information
     */
    @KafkaListener(
        topics = "order-events",
        containerFactory = "orderKafkaListenerContainerFactory",
        groupId = "payment-service-group",
        batch = "true"
    )
    public void consumeOrderCreatedEvents(
            @Payload List<OrderCreatedEvent> events,
            @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions
    ) {
        log.info("Received {} OrderCreatedEvent(s) from partitions {}", events.size(), partitions);

        for (OrderCreatedEvent event : events) {
            try {
                processOrderCreatedEvent(event);
            } catch (Exception e) {
                log.error("Error processing OrderCreatedEvent for order {}: {}",
                    event.orderId, e.getMessage(), e);
                // Continue processing next event
            }
        }
    }

    /**
     * Xử lý một OrderCreatedEvent
     *
     * Flow:
     * 1. Validate event data
     * 2. Check idempotency (tránh xử lý trùng lặp)
     * 3. Tạo Payment record với trạng thái PROCESSING
     * 4. Gọi PaymentService xử lý thanh toán (giả lập Gateway)
     * 5. Publish PaymentProcessedEvent
     *
     * @param event OrderCreatedEvent
     */
    @Transactional
    public void processOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for order ID: {}", event.orderId);

        // Step 1: Validate event
        validateOrderCreatedEvent(event);

        // Step 2: Check idempotency - tránh xử lý trùng lặp
        if (paymentRepository.existsProcessingPaymentForOrder(event.orderId)) {
            log.warn("Payment already processing for order ID: {}. Skipping duplicate event.",
                event.orderId);
            return;
        }

        // Step 3: Tạo Payment record với trạng thái PROCESSING
        Payment payment = createPaymentRecord(event);
        payment = paymentRepository.save(payment);
        log.info("Created Payment record: {} for order: {}",
            payment.paymentNumber, event.orderId);

        // Step 4: Xử lý thanh toán (giả lập gọi Gateway Ngân hàng)
        Payment processedPayment = paymentService.processPayment(payment);

        // Step 5: Publish PaymentProcessedEvent dựa trên kết quả
        PaymentProcessedEvent resultEvent = buildPaymentProcessedEvent(
            processedPayment, event
        );
        paymentEventProducer.sendPaymentProcessedEvent(resultEvent);

        log.info("Completed payment processing for order: {} with status: {}",
            event.orderId, processedPayment.paymentStatus);
    }

    /**
     * Validate OrderCreatedEvent data
     *
     * @param event OrderCreatedEvent
     * @throws IllegalArgumentException nếu data không hợp lệ
     */
    private void validateOrderCreatedEvent(OrderCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("OrderCreatedEvent cannot be null");
        }
        if (event.orderId == null || event.orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (event.finalAmount == null || event.finalAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final amount must be greater than zero");
        }
        if (event.currency == null || event.currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        if (event.customerId == null || event.customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
    }

    /**
     * Tạo Payment entity từ OrderCreatedEvent
     *
     * @param event OrderCreatedEvent
     * @return Payment entity
     */
    private Payment createPaymentRecord(OrderCreatedEvent event) {
        PaymentMethod paymentMethod = PaymentMethod.fromCode(event.paymentMethod);
        PaymentProvider provider = determinePaymentProvider(paymentMethod);

        // Derive username from customer name (first word)
        String username = event.customerName != null
            ? event.customerName.split(" ")[0]
            : event.customerId;

        Payment payment = new Payment();
        payment.orderId = event.orderId;
        payment.userId = event.customerId;
        payment.username = username;
        payment.customerName = event.customerName;
        payment.customerEmail = event.customerEmail;
        payment.customerPhone = event.customerPhone;
        payment.paymentMethod = paymentMethod;
        payment.paymentProvider = provider;
        payment.paymentStatus = PaymentStatus.PROCESSING;
        payment.amount = event.finalAmount;
        payment.currency = event.currency;
        payment.metadata = buildMetadata(event);
        payment.processingStartedAt = LocalDateTime.now();
        payment.retryCount = 0;
        payment.maxRetryAttempts = 3;
        return payment;
    }

    /**
     * Xác định PaymentProvider dựa trên PaymentMethod
     *
     * @param method PaymentMethod
     * @return PaymentProvider
     */
    private PaymentProvider determinePaymentProvider(PaymentMethod method) {
        return switch (method) {
            case VNPAY -> PaymentProvider.VNPAY;
            case MOMO -> PaymentProvider.MOMO;
            case ZALOPAY -> PaymentProvider.ZALOPAY;
            case PAYPAL -> PaymentProvider.PAYPAL;
            case CREDIT_CARD, DEBIT_CARD -> PaymentProvider.STRIPE;
            default -> PaymentProvider.SIMULATION;
        };
    }

    /**
     * Build metadata JSON từ OrderCreatedEvent
     *
     * @param event OrderCreatedEvent
     * @return JSON string
     */
    private String buildMetadata(OrderCreatedEvent event) {
        int itemCount = event.items != null ? event.items.size() : 0;
        return String.format(
            "{\"orderId\":\"%s\",\"orderNumber\":\"%s\",\"eventType\":\"ORDER_CREATED\",\"orderItems\":%d}",
            event.orderId,
            event.orderNumber,
            itemCount
        );
    }

    /**
     * Build PaymentProcessedEvent từ Payment kết quả xử lý
     *
     * @param payment Payment entity sau khi xử lý
     * @param originalEvent OrderCreatedEvent gốc
     * @return PaymentProcessedEvent
     */
    private PaymentProcessedEvent buildPaymentProcessedEvent(
            Payment payment, OrderCreatedEvent originalEvent) {

        boolean success = payment.isPaid();
        String eventType = success
            ? PaymentProcessedEvent.EVENT_TYPE_SUCCESS
            : PaymentProcessedEvent.EVENT_TYPE_FAILED;

        return new PaymentProcessedEvent(
            UUID.randomUUID().toString(),       // eventId
            payment.id.toString(),              // aggregateId (payment ID)
            java.time.Instant.now(),            // timestamp
            originalEvent.customerId,           // userId
            originalEvent.traceId,              // traceId
            originalEvent.orderId,              // orderId
            payment.id.toString(),              // paymentId
            payment.transactionId,              // transactionId
            payment.amount,                     // paidAmount
            payment.currency,                   // currency
            payment.paymentMethod.getCode(),    // paymentMethod
            payment.paymentProvider.getCode(),  // paymentProvider
            success                             // success
        );
    }
}
