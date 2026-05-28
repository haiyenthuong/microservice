package com.order.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order entity representing a customer order.
 * Entity đại diện cho đơn hàng của khách hàng.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_code", length = 50, nullable = false, unique = true)
    private String orderCode;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "cust_name", length = 200)
    private String custName;

    @Column(name = "cust_email", length = 100)
    private String custEmail;

    @Column(name = "cust_phone", length = 20)
    private String custPhone;

    @lombok.Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @lombok.Builder.Default
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @lombok.Builder.Default
    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @lombok.Builder.Default
    @Column(name = "ship_fee", precision = 19, scale = 2)
    private BigDecimal shipFee = BigDecimal.ZERO;

    @Column(name = "ship_addr", length = 500)
    private String shipAddr;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @lombok.Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Payment-related fields
    @Column(name = "payment_status", length = 20)
    private Long paymentStatus;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_failure_reason", length = 500)
    private String paymentFailureReason;

    @Column(name = "currency", length = 3)
    private String currency = "VNĐ";

    /**
     * Lấy trạng thái đơn hàng dưới dạng enum.
     */
    public OrderStatus getOrderStatus() {
        return OrderStatus.fromValue(status);
    }

    /**
     * Thiết lập trạng thái đơn hàng từ enum.
     */
    public void setOrderStatus(OrderStatus orderStatus) {
        if (orderStatus != null) {
            this.status = orderStatus.getValue();
        }
    }

    /**
     * Xử lý đơn hàng.
     */
    public void process() {
        if (!getOrderStatus().isPending()) {
            throw new IllegalStateException("Only pending orders can be processed");
        }
        setOrderStatus(OrderStatus.PROCESSING);
    }

    /**
     * Giao hàng.
     */
    public void ship() {
        OrderStatus currentStatus = getOrderStatus();
        if (currentStatus != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Only processing orders can be shipped");
        }
        setOrderStatus(OrderStatus.SHIPPED);
        this.shippedDate = LocalDateTime.now();
    }

    /**
     * Hoàn tất giao hàng.
     */
    public void deliver() {
        if (!getOrderStatus().isShipped()) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        setOrderStatus(OrderStatus.DELIVERED);
        this.deliveredDate = LocalDateTime.now();
    }

    /**
     * Hủy đơn hàng.
     */
    public void cancel() {
        if (!getOrderStatus().canCancel()) {
            throw new IllegalStateException(
                    "Order cannot be cancelled in current status: " + getOrderStatus().getName());
        }
        setOrderStatus(OrderStatus.CANCELLED);
        this.cancelledDate = LocalDateTime.now();
    }

    /**
     * Tính toán tổng tiền bao gồm items, giảm giá và phí vận chuyển.
     */
    public void calculateTotals() {
        BigDecimal itemsTotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal shipping = shipFee != null ? shipFee : BigDecimal.ZERO;

        this.amount = itemsTotal.add(shipping).subtract(discount);
    }

    /**
     * Thêm sản phẩm vào đơn hàng.
     */
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        item.setOrder(this);
        items.add(item);
        calculateTotals();
    }

    /**
     * Xóa sản phẩm khỏi đơn hàng.
     */
    public void removeItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }

    /**
     * Lấy trạng thái thanh toán dưới dạng enum.
     */
    public PaymentStatus getPaymentStatus() {
        return PaymentStatus.fromValue(paymentStatus);
    }

    /**
     * Thiết lập trạng thái thanh toán từ enum.
     */
    public void setPaymentStatus(PaymentStatus status) {
        if (status != null) {
            this.paymentStatus = status.getValue();
        }
    }

    /**
     * Đánh dấu đơn hàng đã thanh toán.
     *
     * @param transactionId ID giao dịch thanh toán
     */
    public void markAsPaid(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        this.paymentStatus = 2L; // PAID
        this.transactionId = transactionId;
        this.amount = this.amount != null ? this.amount : BigDecimal.ZERO;
    }

    /**
     * Đánh dấu thanh toán thất bại.
     *
     * @param reason Lý do thất bại
     */
    public void markPaymentFailed(String reason) {
        this.paymentStatus = 3L;
        this.paymentFailureReason = reason;
    }

    /**
     * Tạo mã số đơn hàng duy nhất.
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uniqueId;
    }

    /**
     * Tạo mã số đơn hàng trước khi lưu.
     */
    protected void onCreate() {
        super.onCreate();
        if (orderCode == null || orderCode.isEmpty()) {
            orderCode = generateOrderNumber();
        }
        if (status == null) {
            status = 0;
        }
        if (paymentStatus == null) {
            paymentStatus = 0L;
        }
        calculateTotals();
    }
}
