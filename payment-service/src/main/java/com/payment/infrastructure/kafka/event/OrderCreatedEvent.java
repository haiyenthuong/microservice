package com.payment.infrastructure.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Event được consume từ Order Service khi một order được tạo
 *
 * Event này trigger payment processing flow trong Order-Payment Saga:
 * 1. Order Service publish OrderCreatedEvent (event này)
 * 2. Payment Service consume event này
 * 3. Payment Service process payment với payment gateway
 * 4. Payment Service publish PaymentSuccessEvent hoặc PaymentFailedEvent
 * 5. Order Service consume payment event và update order status
 *
 * Event-Carried State Transfer:
 * Event chứa đầy đủ thông tin cần thiết để Payment Service xử lý
 * mà không cần gọi lại Order Service qua REST API.
 */
public class OrderCreatedEvent extends PaymentEvent {

    /**
     * Event type constant
     */
    public static final String EVENT_TYPE = "ORDER_CREATED";

    /**
     * Mã số đơn hàng (unique)
     */
    public String orderNumber;

    /**
     * Order ID (UUID)
     */
    public String orderId;

    /**
     * ID của customer (user) tạo order
     */
    public String customerId;

    /**
     * Tên khách hàng
     */
    public String customerName;

    /**
     * Email khách hàng
     */
    public String customerEmail;

    /**
     * Số điện thoại khách hàng
     */
    public String customerPhone;

    /**
     * Địa chỉ shipping
     */
    public String shippingAddress;

    /**
     * Danh sách items trong order
     */
    public List<OrderItemDto> items;

    /**
     * Tổng tiền hàng (trước discount, tax, shipping)
     */
    public BigDecimal totalAmount;

    /**
     * Số tiền giảm giá
     */
    public BigDecimal discountAmount;

    /**
     * Số tiền thuế
     */
    public BigDecimal taxAmount;

    /**
     * Phí vận chuyển
     */
    public BigDecimal shippingAmount;

    /**
     * Tổng số tiền cần thanh toán (final)
     */
    public BigDecimal finalAmount;

    /**
     * Currency code (USD, VND, etc.)
     */
    public String currency;

    /**
     * Payment method được chọn (CREDIT_CARD, PAYPAL, etc.)
     */
    public String paymentMethod;

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
            String orderId,
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
        super(eventId, EVENT_TYPE, aggregateId, timestamp, userId, traceId);
        this.orderNumber = orderNumber;
        this.orderId = orderId;
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
     * DTO cho Order Item
     * Chứa đầy đủ thông tin về sản phẩm trong order
     */
    public static class OrderItemDto {
        public String productId;
        public String productName;
        public String productSku;
        public String productImage;
        public Integer quantity;
        public BigDecimal unitPrice;
        public BigDecimal discountAmount;
        public BigDecimal taxAmount;
        public BigDecimal totalPrice;
        public String currency;

        public OrderItemDto() {
        }
    }
}
