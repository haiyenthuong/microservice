package com.order.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Order Item entity representing a product in an order.
 * Entity đại diện cho một sản phẩm trong đơn hàng.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "product_name", length = 255, nullable = false)
    private String productName;

    @Column(name = "product_sku", length = 100)
    private String productSku;

    @Column(name = "product_image", length = 500)
    private String productImage;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Tính toán tổng giá dựa trên đơn giá, số lượng, giảm giá và thuế.
     */
    public void calculateTotalPrice() {
        Integer qty = quantity != null && quantity > 0 ? quantity : 1;
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;

        this.totalPrice = subtotal.subtract(discount).add(tax);
    }

    /**
     * Cập nhật số lượng và tính lại tổng giá.
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = newQuantity;
        calculateTotalPrice();
    }

    /**
     * Tính tổng giá trước khi lưu hoặc cập nhật.
     */
    @PrePersist
    @PreUpdate
    protected void onBeforeSave() {
        calculateTotalPrice();
    }

    /**
     * Thiết lập đơn hàng và duy trì mối quan hệ hai chiều.
     */
    public void setOrder(Order order) {
        this.order = order;
        if (order != null && !order.getItems().contains(this)) {
            order.getItems().add(this);
        }
    }
}
