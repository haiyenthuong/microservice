package com.order.application.query;

import com.order.application.dto.OrderItemResponse;
import com.order.application.dto.OrderResponse;
import com.order.domain.model.Order;
import com.order.domain.model.OrderStatus;
import com.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query để lấy tất cả orders.
 */
@Component
@RequiredArgsConstructor
public class GetAllOrdersQuery implements IQuery {

    private final OrderRepository orderRepository;

    /**
     * Execute query để lấy tất cả orders.
     *
     * @return list of OrderResponse
     */
    public List<OrderResponse> execute() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Order entity sang OrderResponse DTO.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        OrderStatus status = order.getOrderStatus();

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .status(order.getStatus())
                .statusName(status.getName())
                .statusDescription(status.getDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .finalAmount(order.getFinalAmount())
                .currency(order.getCurrency())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .customerNotes(order.getCustomerNotes())
                .adminNotes(order.getAdminNotes())
                .orderDate(order.getOrderDate())
                .confirmedDate(order.getConfirmedDate())
                .shippedDate(order.getShippedDate())
                .deliveredDate(order.getDeliveredDate())
                .cancelledDate(order.getCancelledDate())
                .items(itemResponses)
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .createdDate(order.getCreatedDate())
                .updatedDate(order.getUpdatedDate())
                .build();
    }

    /**
     * Map OrderItem entity sang OrderItemResponse DTO.
     */
    private OrderItemResponse mapToOrderItemResponse(com.order.domain.model.OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .totalPrice(item.getTotalPrice())
                .currency(item.getCurrency())
                .createdDate(item.getCreatedDate())
                .updatedDate(item.getUpdatedDate())
                .build();
    }
}
