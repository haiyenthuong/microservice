package com.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho Order responses.
 * DTO dùng cho phản hồi thông tin order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer status;
    private String statusName;
    private String statusDescription;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal finalAmount;
    private String currency;
    private String shippingAddress;
    private String billingAddress;
    private String customerNotes;
    private String adminNotes;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    private Integer paymentStatus;
    private String paymentStatusName;
    private String paymentStatusDescription;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String paymentFailureReason;
    private List<OrderItemResponse> items;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
