package com.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO để tạo mới một đơn hàng.
 * DTO dùng cho yêu cầu tạo thông tin order.
 *
 * REFACTORED (Phase 3):
 * - Removed userId field (được lấy từ SecurityHelper thay vì từ request)
 * - Thêm validation annotations
 * - Thêm documentation cho các fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /**
     * Tên khách hàng
     * Optional - nếu không provided, sẽ dùng username từ SecurityHelper
     */
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    /**
     * Email khách hàng
     */
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;

    /**
     * Số điện thoại khách hàng
     */
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String customerPhone;

    /**
     * Danh sách sản phẩm trong đơn hàng
     * Required - phải có ít nhất 1 item
     */
    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> items;

    /**
     * Địa chỉ giao hàng
     * Required cho physical products
     */
    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;

    /**
     * Địa chỉ billing (địa chỉ xuất hóa đơn)
     * Optional - nếu không provided, sẽ dùng shipping address
     */
    @Size(max = 500, message = "Billing address must not exceed 500 characters")
    private String billingAddress;

    /**
     * Số tiền giảm giá (đơn hàng level)
     * Áp dụng cho tổng đơn hàng
     */
    @DecimalMin(value = "0.0", message = "Discount amount must be greater than or equal to 0")
    private BigDecimal discountAmount;

    /**
     * Số tiền thuế (đơn hàng level)
     * Áp dụng cho tổng đơn hàng
     */
    @DecimalMin(value = "0.0", message = "Tax amount must be greater than or equal to 0")
    private BigDecimal taxAmount;

    /**
     * Phí vận chuyển
     */
    @DecimalMin(value = "0.0", message = "Shipping amount must be greater than or equal to 0")
    private BigDecimal shippingAmount;

    /**
     * Currency code (USD, VND, EUR, etc.)
     * Default: USD
     */
    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency;

    /**
     * Phương thức thanh toán
     * Required - CREDIT_CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY, etc.
     */
    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    /**
     * Ghi chú của khách hàng
     */
    @Size(max = 1000, message = "Customer notes must not exceed 1000 characters")
    private String customerNotes;

    /**
     * DTO cho order item trong request tạo đơn hàng.
     * DTO dùng cho thông tin sản phẩm trong đơn hàng.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        /**
         * Product ID
         * Required - ID của product từ product catalog
         */
        @NotBlank(message = "Product ID is required")
        @Size(max = 36, message = "Product ID must not exceed 36 characters")
        private String productId;

        /**
         * Product Name
         * Required - Tên hiển thị của product
         */
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        private String productName;

        /**
         * Product SKU (Stock Keeping Unit)
         * Mã sản phẩm dùng cho inventory management
         */
        @Size(max = 100, message = "Product SKU must not exceed 100 characters")
        private String productSku;

        /**
         * Product Image URL
         */
        @Size(max = 500, message = "Product image URL must not exceed 500 characters")
        private String productImage;

        /**
         * Số lượng ordered
         * Required - Phải >= 1
         */
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "1.0", message = "Quantity must be at least 1")
        private Integer quantity;

        /**
         * Đơn giá per unit
         * Required - Phải >= 0
         */
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;

        /**
         * Discount amount cho item này
         * Giảm giá per unit hoặc per item
         */
        @DecimalMin(value = "0.0", message = "Discount amount must be greater than or equal to 0")
        private BigDecimal discountAmount;

        /**
         * Tax amount cho item này
         * Thuế per unit hoặc per item
         */
        @DecimalMin(value = "0.0", message = "Tax amount must be greater than or equal to 0")
        private BigDecimal taxAmount;

        /**
         * Currency code cho item
         * Default: USD (hoặc sẽ inherit từ order level currency)
         */
        @Size(max = 3, message = "Currency code must not exceed 3 characters")
        private String currency;
    }

    /**
     * Helper method để get billing address
     * Trả về billing address nếu có, ngược lại trả về shipping address
     *
     * @return billing address hoặc shipping address
     */
    public String getEffectiveBillingAddress() {
        return (billingAddress != null && !billingAddress.trim().isEmpty())
                ? billingAddress
                : shippingAddress;
    }

    /**
     * Helper method để get customer name
     * Trả về customer name nếu có, ngược lại trả về default value
     *
     * @return customer name hoặc "Guest Customer"
     */
    public String getEffectiveCustomerName() {
        return (customerName != null && !customerName.trim().isEmpty())
                ? customerName
                : "Guest Customer";
    }

    /**
     * Helper method để get currency
     * Trả về currency nếu có, ngược lại trả về default "USD"
     *
     * @return currency code hoặc "USD"
     */
    public String getEffectiveCurrency() {
        return (currency != null && !currency.trim().isEmpty())
                ? currency
                : "USD";
    }

    /**
     * Validate request trước khi xử lý
     *
     * @throws IllegalArgumentException nếu request không hợp lệ
     */
    public void validate() {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        for (OrderItemRequest item : items) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID is required for all items");
            }

            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1 for all items");
            }

            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price must be greater than or equal to 0 for all items");
            }
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
    }
}
