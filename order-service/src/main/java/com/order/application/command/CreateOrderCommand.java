package com.order.application.command;

import com.order.application.dto.CreateOrderRequest;
import com.order.application.dto.OrderItemResponse;
import com.order.application.dto.OrderResponse;
import com.order.domain.model.Order;
import com.order.domain.model.OrderItem;
import com.order.domain.model.OrderStatus;
import com.order.domain.repository.OrderRepository;
import com.order.infrastructure.helper.SecurityHelper;
import com.order.infrastructure.kafka.event.OrderCreatedEvent;
import com.order.infrastructure.kafka.producer.OrderEventProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command để tạo mới một order theo kiến trúc Event-Driven
 *
 * REFACTORED (Phase 3):
 * - Không gọi Payment Service trực tiếp qua Feign Client
 * - Lưu order với status PENDING
 * - Publish OrderCreatedEvent đến Kafka
 * - Trả về response ngay lập tức (async payment processing)
 *
 * Flow:
 * 1. Validate request
 * 2. Lấy userId từ SecurityHelper (đã set bởi UserHeaderInterceptor từ Gateway)
 * 3. Create Order entity với đầy đủ thông tin
 * 4. Create OrderItems và calculate totals
 * 5. Save order xuống database (trong transaction)
 * 6. Publish OrderCreatedEvent SAU KHI transaction commit
 * 7. Return response về client ngay lập tức
 *
 * Payment flow (async, background):
 * - Payment Service consume OrderCreatedEvent
 * - Payment Service process payment với payment provider
 * - Payment Service publish PaymentSuccessEvent hoặc PaymentFailedEvent
 * - Order Service consume payment event và update order status
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderCommand implements ICommand {

    private final OrderRepository orderRepository;
    private final OrderEventProducerService orderEventProducerService;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Execute command để tạo order mới theo kiến trúc Event-Driven
     *
     * @param request DTO chứa thông tin order
     * @return OrderResponse với status PENDING (payment sẽ được xử lý async)
     * @throws IllegalStateException    nếu user chưa authenticated
     * @throws IllegalArgumentException nếu request không hợp lệ
     */
    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
        // Validate authentication
        securityHelper.requireAuthenticated();

        // Lấy current user ID từ SecurityHelper (đã set bởi UserHeaderInterceptor từ
        // Gateway)
        String currentUserId = securityHelper.getCurrentUserId();
        String currentUsername = securityHelper.getCurrentUsername();

        log.info("Creating order for user: {} ({})", currentUsername, currentUserId);

        // Validate request
        validateRequest(request);

        // Create Order entity
        Order order = buildOrderEntity(request, currentUserId, currentUsername);

        // Create OrderItems và link với Order
        List<OrderItem> orderItems = buildOrderItems(request, order);
        order.setItems(orderItems);

        // Calculate totals (items total, discount, tax, shipping, final amount)
        calculateOrderTotals(order);

        // Save order xuống database (trong transaction)
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully: {} | Order Number: {} | Status: {} | Amount: {}",
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getOrderStatus().getName(),
                savedOrder.getAmount());

        // Publish OrderCreatedEvent SAU KHI transaction commit
        // Sử dụng TransactionSynchronization để đảm bảo event chỉ được publish
        // khi transaction đã commit thành công (để avoid duplicate events)
        publishOrderCreatedEventAfterTransactionCommit(savedOrder, currentUserId);

        // Map sang response và return ngay lập tức
        // Payment sẽ được xử lý async bởi Payment Service
        return mapToOrderResponse(savedOrder);
    }

    /**
     * Validate CreateOrderRequest
     *
     * @param request Request cần validate
     * @throws IllegalArgumentException nếu request không hợp lệ
     */
    private void validateRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateOrderRequest cannot be null");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }

        // Validate các items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null || itemRequest.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID is required for all items");
            }

            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1 for all items");
            }

            if (itemRequest.getUnitPrice() == null || itemRequest.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price must be greater than or equal to 0 for all items");
            }
        }
    }

    /**
     * Build Order entity từ CreateOrderRequest
     *
     * @param request  CreateOrderRequest
     * @param userId   User ID từ SecurityHelper
     * @param username Username từ SecurityHelper
     * @return Order entity (chưa save)
     */
    private Order buildOrderEntity(CreateOrderRequest request, String userId, String username) {
        // Normalize các amount fields
        BigDecimal discountAmount = request.getDiscountAmount() != null
                ? request.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal shipFee = request.getShipFee() != null
                ? request.getShipFee()
                : BigDecimal.ZERO;

        // Build Order entity
        return Order.builder()
                // Customer info
                .userId(userId)
                .custName(request.getCustName() != null ? request.getCustName().trim() : username)
                .custEmail(request.getCustEmail() != null ? request.getCustEmail().trim() : null)
                .custPhone(request.getCustPhone() != null ? request.getCustPhone().trim() : null)

                // Address
                .shipAddr(request.getShipAddr() != null ? request.getShipAddr().trim() : null)

                // Amounts
                .discountAmount(discountAmount)
                .shipFee(shipFee)

                // Notes
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)

                // Order status - Initial status là PENDING
                .status(OrderStatus.PENDING.getValue())

                // Audit info
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }

    /**
     * Build OrderItem entities từ CreateOrderRequest
     *
     * @param request CreateOrderRequest
     * @param order   Order entity (cha)
     * @return List của OrderItem entities
     */
    private List<OrderItem> buildOrderItems(CreateOrderRequest request, Order order) {
        List<OrderItem> items = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Normalize item fields
            BigDecimal discountAmount = itemRequest.getDiscountAmount() != null
                    ? itemRequest.getDiscountAmount()
                    : BigDecimal.ZERO;

            BigDecimal taxAmount = itemRequest.getTaxAmount() != null
                    ? itemRequest.getTaxAmount()
                    : BigDecimal.ZERO;

            // Build OrderItem entity
            OrderItem item = OrderItem.builder()
                    .productId(itemRequest.getProductId().trim())
                    .productName(itemRequest.getProductName() != null ? itemRequest.getProductName().trim()
                            : "Unknown Product")
                    .productSku(itemRequest.getProductSku() != null ? itemRequest.getProductSku().trim() : null)
                    .productImage(itemRequest.getProductImage() != null ? itemRequest.getProductImage().trim() : null)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .discountAmount(discountAmount)
                    .taxAmount(taxAmount)
                    // Audit info
                    .createdBy(order.getCreatedBy())
                    .updatedBy(order.getUpdatedBy())
                    .build();

            // Set relationship with Order
            item.setOrder(order);

            items.add(item);
        }

        return items;
    }

    /**
     * Calculate totals cho Order
     *
     * Logic tính toán:
     * 1. Items Total = Sum của (quantity * unitPrice) cho tất cả items
     * 2. Amount = Items Total + Ship Fee - Discount Amount
     *
     * Lưu ý: Mỗi OrderItem có thể có riêng discount và tax,
     * và totalPrice của item được tính bởi item.calculateTotalPrice()
     *
     * @param order Order entity cần calculate totals
     */
    private void calculateOrderTotals(Order order) {
        BigDecimal itemsTotal = BigDecimal.ZERO;
        BigDecimal itemsDiscountTotal = BigDecimal.ZERO;

        // Calculate totals từ items
        for (OrderItem item : order.getItems()) {
            // Calculate item total price (đã bao gồm discount và tax của item)
            item.calculateTotalPrice();

            // Accumulate totals
            BigDecimal itemTotalPrice = item.getTotalPrice() != null
                    ? item.getTotalPrice()
                    : BigDecimal.ZERO;

            itemsTotal = itemsTotal.add(itemTotalPrice);

            // Accumulate item-level discounts
            if (item.getDiscountAmount() != null) {
                itemsDiscountTotal = itemsDiscountTotal.add(item.getDiscountAmount());
            }
        }

        // Get order-level amounts
        BigDecimal orderDiscount = order.getDiscountAmount() != null
                ? order.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal orderShipFee = order.getShipFee() != null
                ? order.getShipFee()
                : BigDecimal.ZERO;

        // Combine item-level và order-level amounts
        BigDecimal totalDiscount = itemsDiscountTotal.add(orderDiscount);

        // Calculate final amount
        // Amount = Items Total + Ship Fee - Total Discount
        BigDecimal amount = itemsTotal.add(orderShipFee).subtract(totalDiscount);

        // Ensure amount không negative
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }

        // Round to 2 decimal places (standard cho currency)
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        // Set values to order
        order.setAmount(amount);

        log.debug("Order totals calculated - Items Total: {} | Discount: {} | Ship Fee: {} | Amount: {}",
                itemsTotal, totalDiscount, orderShipFee, amount);
    }

    /**
     * Publish OrderCreatedEvent sau khi transaction commit
     *
     * Sử dụng TransactionSynchronization để ensure event chỉ được publish
     * khi transaction đã commit thành công.
     *
     * Điều này đảm bảo:
     * - Nếu transaction rollback → event KHÔNG được publish
     * - Nếu transaction commit → event được publish
     *
     * @param order  Order entity đã được save
     * @param userId User ID tạo order
     */
    private void publishOrderCreatedEventAfterTransactionCommit(Order order, String userId) {
        // Register transaction synchronization callback
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // Transaction đã commit thành công, publish event
                    log.info("Transaction committed, publishing OrderCreatedEvent for order: {}", order.getId());

                    // Build OrderCreatedEvent
                    OrderCreatedEvent event = buildOrderCreatedEvent(order, userId);

                    // Publish event qua ApplicationEventPublisher
                    // OrderEventProducerService sẽ listen và publish to Kafka
                    eventPublisher.publishEvent(event);

                } catch (Exception e) {
                    log.error("Error publishing OrderCreatedEvent for order: {}", order.getId(), e);
                    // TODO: Implement retry mechanism hoặc Dead Letter Queue
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.warn("Transaction rolled back, OrderCreatedEvent NOT published for order: {}", order.getId());
                }
            }
        });
    }

    /**
     * Build OrderCreatedEvent từ Order entity
     *
     * @param order  Order entity
     * @param userId User ID
     * @return OrderCreatedEvent
     */
    private OrderCreatedEvent buildOrderCreatedEvent(Order order, String userId) {
        // Convert OrderItems sang DTO
        List<OrderCreatedEvent.OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());

        // Generate trace ID
        String traceId = UUID.randomUUID().toString();

        // Build event sử dụng builder pattern
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateId(order.getId()) // Order ID là aggregate ID
                .timestamp(Instant.now())
                .userId(userId)
                .traceId(traceId)
                .orderNumber(order.getOrderCode())
                .customerId(order.getUserId())
                .customerName(order.getCustName())
                .customerEmail(order.getCustEmail())
                .customerPhone(order.getCustPhone())
                .shippingAddress(order.getShipAddr())
                .items(itemDtos)
                .amount(order.getAmount())
                .discountAmount(order.getDiscountAmount())
                .shipFee(order.getShipFee())
                .build();
    }

    /**
     * Map OrderItem entity sang OrderItemDto cho event
     *
     * @param item OrderItem entity
     * @return OrderItemDto
     */
    private OrderCreatedEvent.OrderItemDto mapToOrderItemDto(OrderItem item) {
        return new OrderCreatedEvent.OrderItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getProductSku(),
                item.getProductImage(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountAmount(),
                item.getTaxAmount(),
                item.getTotalPrice(),
                "VND"); // Default currency for VNPAY payment
    }

    /**
     * Map Order entity sang OrderResponse DTO
     *
     * @param order Order entity
     * @return OrderResponse
     */
    private OrderResponse mapToOrderResponse(Order order) {
        OrderStatus status = order.getOrderStatus();

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderCode())
                .userId(order.getUserId())
                .custName(order.getCustName())
                .custEmail(order.getCustEmail())
                .custPhone(order.getCustPhone())
                .status(order.getStatus())
                .statusName(status.getName())
                .statusDescription(status.getDescription())
                .amount(order.getAmount())
                .discountAmount(order.getDiscountAmount())
                .shipFee(order.getShipFee())
                .shipAddr(order.getShipAddr())
                .notes(order.getNotes())
                .shippedDate(order.getShippedDate())
                .deliveredDate(order.getDeliveredDate())
                .cancelledDate(order.getCancelledDate())
                .items(itemResponses)
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .createdDate(order.getCreatedDate())
                .updatedDate(order.getUpdatedDate())
                .build();
    }

    /**
     * Map OrderItem entity sang OrderItemResponse DTO
     *
     * @param item OrderItem entity
     * @return OrderItemResponse
     */
    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .totalPrice(item.getTotalPrice())
                .createdDate(item.getCreatedDate())
                .updatedDate(item.getUpdatedDate())
                .build();
    }
}
