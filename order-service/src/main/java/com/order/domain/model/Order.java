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

    @Column(name = "order_number", length = 50, nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", precision = 19, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "billing_address", length = 500)
    private String billingAddress;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "payment_status")
    private Integer paymentStatus = 0; // PaymentStatus.PENDING

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_failure_reason", length = 500)
    private String paymentFailureReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

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
     * Lấy trạng thái thanh toán dưới dạng enum.
     */
    public PaymentStatus getPaymentStatus() {
        return PaymentStatus.fromValue(paymentStatus);
    }

    /**
     * Thiết lập trạng thái thanh toán từ enum.
     */
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus != null) {
            this.paymentStatus = paymentStatus.getValue();
        }
    }

    /**
     * Đánh dấu đơn hàng đã thanh toán thành công.
     */
    public void markAsPaid(String transactionId) {
        setPaymentStatus(PaymentStatus.PAID);
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    /**
     * Đánh dấu thanh toán thất bại.
     */
    public void markPaymentFailed(String reason) {
        setPaymentStatus(PaymentStatus.FAILED);
        this.paymentFailureReason = reason;
    }

    /**
     * Xác nhận đơn hàng.
     */
    public void confirm() {
        if (!getOrderStatus().isPending()) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        setOrderStatus(OrderStatus.CONFIRMED);
        this.confirmedDate = LocalDateTime.now();
    }

    /**
     * Xử lý đơn hàng.
     */
    public void process() {
        if (!getOrderStatus().isConfirmed()) {
            throw new IllegalStateException("Only confirmed orders can be processed");
        }
        setOrderStatus(OrderStatus.PROCESSING);
    }

    /**
     * Giao hàng.
     */
    public void ship() {
        OrderStatus currentStatus = getOrderStatus();
        if (currentStatus != OrderStatus.CONFIRMED && currentStatus != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Only confirmed or processing orders can be shipped");
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
            throw new IllegalStateException("Order cannot be cancelled in current status: " + getOrderStatus().getName());
        }
        setOrderStatus(OrderStatus.CANCELLED);
        this.cancelledDate = LocalDateTime.now();
    }

    /**
     * Tính toán tổng tiền bao gồm items, giảm giá, thuế, và phí vận chuyển.
     */
    public void calculateTotals() {
        BigDecimal itemsTotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = itemsTotal;

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        BigDecimal shipping = shippingAmount != null ? shippingAmount : BigDecimal.ZERO;

        this.finalAmount = totalAmount
                .subtract(discount)
                .add(tax)
                .add(shipping);
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
     * Tạo mã số đơn hàng duy nhất.
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uniqueId;
    }

    /**
     * Tạo mã số đơn hàng và thiết lập ngày đặt hàng trước khi lưu.
     */
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (orderNumber == null || orderNumber.isEmpty()) {
            orderNumber = generateOrderNumber();
        }
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = 0;
        }
        calculateTotals();
    }
}
