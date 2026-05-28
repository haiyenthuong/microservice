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
    private String custName;

    /**
     * Email khách hàng
     */
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String custEmail;

    /**
     * Số điện thoại khách hàng
     */
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String custPhone;

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
    private String shipAddr;

    /**
     * Số tiền giảm giá (đơn hàng level)
     * Áp dụng cho tổng đơn hàng
     */
    @DecimalMin(value = "0.0", message = "Discount amount must be greater than or equal to 0")
    private BigDecimal discountAmount;

    /**
     * Phí vận chuyển
     */
    @DecimalMin(value = "0.0", message = "Shipping fee must be greater than or equal to 0")
    private BigDecimal shipFee;

    /**
     * Ghi chú của khách hàng
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

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
    }

    /**
     * Helper method để get customer name
     * Trả về customer name nếu có, ngược lại trả về default value
     *
     * @return customer name hoặc "Guest Customer"
     */
    public String getEffectiveCustomerName() {
        return (custName != null && !custName.trim().isEmpty())
                ? custName
                : "Guest Customer";
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

        if (shipAddr == null || shipAddr.trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
    }
}
