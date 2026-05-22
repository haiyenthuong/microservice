package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event được publish khi Order được tạo thành công
 *
 * Event này trigger payment flow trong Order-Payment Saga:
 * 1. Order Service publish OrderCreatedEvent (event này)
 * 2. Payment Service consume và process payment
 * 3. Payment Service publish PaymentSuccessEvent hoặc PaymentFailedEvent
 * 4. Order Service consume và update order status
 *
 * Event-Carried State Transfer:
 * Event chứa đầy đủ thông tin cần thiết để Payment Service xử lý
 * mà không cần gọi lại Order Service qua REST API.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent extends OrderEvent {

    /**
     * Event type constant
     */
    public static final String EVENT_TYPE = "ORDER_CREATED";

    /**
     * Constructor đầy đủ
     */
    public OrderCreatedEvent(
            String eventId,
            String aggregateId,
            Instant timestamp,
            String userId,
            String traceId,
            String orderNumber,
            String customerId,
            String customerName,
            String customerEmail,
            String customerPhone,
            String shippingAddress,
            List<OrderItemDto> items,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal shippingAmount,
            BigDecimal finalAmount,
            String currency,
            String paymentMethod
    ) {
        super();
        setEventId(eventId);
        setEventType(EVENT_TYPE);
        setAggregateId(aggregateId);
        setTimestamp(timestamp);
        setUserId(userId);
        setTraceId(traceId);
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.shippingAmount = shippingAmount;
        this.finalAmount = finalAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
    }

    /**
     * Mã số đơn hàng (unique)
     */
    private String orderNumber;

    /**
     * ID của customer (user) tạo order
     */
    private String customerId;

    /**
     * Tên khách hàng
     */
    private String customerName;

    /**
     * Email khách hàng
     */
    private String customerEmail;

    /**
     * Số điện thoại khách hàng
     */
    private String customerPhone;

    /**
     * Địa chỉ shipping
     */
    private String shippingAddress;

    /**
     * Danh sách items trong order
     */
    private List<OrderItemDto> items;

    /**
     * Tổng tiền hàng (trước discount, tax, shipping)
     */
    private BigDecimal totalAmount;

    /**
     * Số tiền giảm giá
     */
    private BigDecimal discountAmount;

    /**
     * Số tiền thuế
     */
    private BigDecimal taxAmount;

    /**
     * Phí vận chuyển
     */
    private BigDecimal shippingAmount;

    /**
     * Tổng số tiền cần thanh toán (final)
     */
    private BigDecimal finalAmount;

    /**
     * Currency code (USD, VND, etc.)
     */
    private String currency;

    /**
     * Payment method được chọn (CREDIT_CARD, PAYPAL, etc.)
     */
    private String paymentMethod;

    /**
     * DTO cho Order Item
     * Chứa đầy đủ thông tin về sản phẩm trong order
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        /**
         * Product ID
         */
        private String productId;

        /**
         * Product Name
         */
        private String productName;

        /**
         * Product SKU (Stock Keeping Unit)
         */
        private String productSku;

        /**
         * Product Image URL
         */
        private String productImage;

        /**
         * Số lượng ordered
         */
        private Integer quantity;

        /**
         * Đơn giá per unit
         */
        private BigDecimal unitPrice;

        /**
         * Discount amount cho item này
         */
        private BigDecimal discountAmount;

        /**
         * Tax amount cho item này
         */
        private BigDecimal taxAmount;

        /**
         * Tổng giá cho item này (quantity * unitPrice - discount + tax)
         */
        private BigDecimal totalPrice;

        /**
         * Currency code cho item
         */
        private String currency;
    }

    /**
     * Builder pattern helper method
     * Tạo OrderCreatedEvent từ các parameters
     */
    public static OrderCreatedEventBuilder builder() {
        return new OrderCreatedEventBuilder();
    }

    /**
     * Builder class cho OrderCreatedEvent
     */
    public static class OrderCreatedEventBuilder {
        private String eventId;
        private String aggregateId;
        private Instant timestamp;
        private String userId;
        private String traceId;
        private String orderNumber;
        private String customerId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String shippingAddress;
        private List<OrderItemDto> items;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal shippingAmount;
        private BigDecimal finalAmount;
        private String currency;
        private String paymentMethod;

        public OrderCreatedEventBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public OrderCreatedEventBuilder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public OrderCreatedEventBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public OrderCreatedEventBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public OrderCreatedEventBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public OrderCreatedEventBuilder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public OrderCreatedEventBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public OrderCreatedEventBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public OrderCreatedEventBuilder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public OrderCreatedEventBuilder customerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
            return this;
        }

        public OrderCreatedEventBuilder shippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public OrderCreatedEventBuilder items(List<OrderItemDto> items) {
            this.items = items;
            return this;
        }

        public OrderCreatedEventBuilder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public OrderCreatedEventBuilder discountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public OrderCreatedEventBuilder taxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        public OrderCreatedEventBuilder shippingAmount(BigDecimal shippingAmount) {
            this.shippingAmount = shippingAmount;
            return this;
        }

        public OrderCreatedEventBuilder finalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
            return this;
        }

        public OrderCreatedEventBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public OrderCreatedEventBuilder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        /**
         * Build OrderCreatedEvent instance
         * Auto-generate eventId và timestamp nếu chưa có
         */
        public OrderCreatedEvent build() {
            // Auto-generate eventId nếu chưa có
            if (this.eventId == null || this.eventId.isEmpty()) {
                this.eventId = UUID.randomUUID().toString();
            }

            // Auto-set timestamp nếu chưa có
            if (this.timestamp == null) {
                this.timestamp = Instant.now();
            }

            return new OrderCreatedEvent(
                    eventId,
                    aggregateId,
                    timestamp,
                    userId,
                    traceId,
                    orderNumber,
                    customerId,
                    customerName,
                    customerEmail,
                    customerPhone,
                    shippingAddress,
                    items,
                    totalAmount,
                    discountAmount,
                    taxAmount,
                    shippingAmount,
                    finalAmount,
                    currency,
                    paymentMethod
            );
        }
    }
}
