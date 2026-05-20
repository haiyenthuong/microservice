package com.order.application.command;

import com.order.application.dto.CreateOrderRequest;
import com.order.application.dto.OrderItemResponse;
import com.order.application.dto.OrderResponse;
import com.order.domain.model.Order;
import com.order.domain.model.OrderItem;
import com.order.domain.model.OrderStatus;
import com.order.domain.model.PaymentStatus;
import com.order.domain.repository.OrderRepository;
import com.order.infrastructure.client.PaymentServiceClient;
import com.order.infrastructure.helper.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command để tạo mới một order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderCommand implements ICommand {

    private final OrderRepository orderRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final SecurityHelper securityHelper;

    /**
     * Execute command để tạo order mới.
     *
     * @param request DTO chứa thông tin order
     * @param currentUserId ID của user đang thực hiện hành động
     * @return OrderResponse
     */
    @Transactional
    public OrderResponse execute(CreateOrderRequest request, String currentUserId) {
        // Lấy userId từ JWT token (security helper) thay vì parameter
        String userIdFromToken = securityHelper.getCurrentUserId();
        if (userIdFromToken != null) {
            currentUserId = userIdFromToken;
        }

        log.info("Creating order for user: {}", currentUserId);

        // Create order với payment status PENDING
        Order order = Order.builder()
                .userId(currentUserId)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .shippingAmount(request.getShippingAmount() != null ? request.getShippingAmount() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .customerNotes(request.getCustomerNotes())
                .paymentStatus(PaymentStatus.PENDING.getValue())
                .paymentMethod(request.getPaymentMethod())
                .status(0)
                .createdBy(currentUserId)
                .build();

        // Add order items
        List<OrderItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = OrderItem.builder()
                        .productId(itemRequest.getProductId())
                        .productName(itemRequest.getProductName())
                        .productSku(itemRequest.getProductSku())
                        .productImage(itemRequest.getProductImage())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(itemRequest.getUnitPrice())
                        .discountAmount(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO)
                        .taxAmount(itemRequest.getTaxAmount() != null ? itemRequest.getTaxAmount() : BigDecimal.ZERO)
                        .currency(itemRequest.getCurrency() != null ? itemRequest.getCurrency() : "USD")
                        .build();
                item.setOrder(order);
                items.add(item);
            }
        }

        order.setItems(items);
        order.calculateTotals();

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Process payment via payment-service
        try {
            log.info("Processing payment for order: {}", savedOrder.getId());

            PaymentServiceClient.ProcessPaymentRequest paymentRequest =
                    PaymentServiceClient.ProcessPaymentRequest.builder()
                            .orderId(savedOrder.getId())
                            .userId(currentUserId)
                            .amount(savedOrder.getFinalAmount())
                            .currency(savedOrder.getCurrency())
                            .paymentMethod(request.getPaymentMethod())
                            .build();

            // FeignClient sẽ tự động thêm Authorization header qua FeignConfig
            PaymentServiceClient.PaymentResponse paymentResponse = paymentServiceClient.processPayment(paymentRequest, null);

            // Update order payment status
            if ("PAID".equals(paymentResponse.getPaymentStatusName())) {
                savedOrder.markAsPaid(paymentResponse.getTransactionId());
                log.info("Payment successful for order: {}, transaction ID: {}", savedOrder.getId(), paymentResponse.getTransactionId());
            } else {
                savedOrder.markPaymentFailed(paymentResponse.getFailureReason());
                log.warn("Payment failed for order: {}", savedOrder.getId());
            }

            savedOrder = orderRepository.save(savedOrder);

        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", savedOrder.getId(), e);
            savedOrder.markPaymentFailed("Payment service error: " + e.getMessage());
            savedOrder = orderRepository.save(savedOrder);
        }

        return mapToOrderResponse(savedOrder);
    }

    /**
     * Map Order entity sang OrderResponse DTO.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        OrderStatus status = order.getOrderStatus();
        PaymentStatus paymentStatus = order.getPaymentStatus();

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
                .paymentStatus(order.getPaymentStatus().getValue())
                .paymentStatusName(paymentStatus.getName())
                .paymentStatusDescription(paymentStatus.getDescription())
                .paymentMethod(order.getPaymentMethod())
                .transactionId(order.getTransactionId())
                .paymentDate(order.getPaymentDate())
                .paymentFailureReason(order.getPaymentFailureReason())
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
    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
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
