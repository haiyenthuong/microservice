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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 36, message = "User ID must not exceed 36 characters")
    private String userId;

    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String customerPhone;

    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;

    @Size(max = 500, message = "Billing address must not exceed 500 characters")
    private String billingAddress;

    @DecimalMin(value = "0.0", message = "Discount amount must be greater than or equal to 0")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", message = "Tax amount must be greater than or equal to 0")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0", message = "Shipping amount must be greater than or equal to 0")
    private BigDecimal shippingAmount;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

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

        @NotBlank(message = "Product ID is required")
        @Size(max = 36, message = "Product ID must not exceed 36 characters")
        private String productId;

        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        private String productName;

        @Size(max = 100, message = "Product SKU must not exceed 100 characters")
        private String productSku;

        @Size(max = 500, message = "Product image URL must not exceed 500 characters")
        private String productImage;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "1.0", message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;

        @DecimalMin(value = "0.0", message = "Discount amount must be greater than or equal to 0")
        private BigDecimal discountAmount;

        @DecimalMin(value = "0.0", message = "Tax amount must be greater than or equal to 0")
        private BigDecimal taxAmount;

        @Size(max = 3, message = "Currency code must not exceed 3 characters")
        private String currency;
    }
}
