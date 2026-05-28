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
    private String custName;
    private String custEmail;
    private String custPhone;
    private Integer status;
    private String statusName;
    private String statusDescription;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal shipFee;
    private String shipAddr;
    private String notes;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    private List<OrderItemResponse> items;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
