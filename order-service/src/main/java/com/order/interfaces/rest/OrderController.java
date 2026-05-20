package com.order.interfaces.rest;

import com.order.application.command.CreateOrderCommand;
import com.order.application.command.UpdatePaymentStatusCommand;
import com.order.application.dto.CreateOrderRequest;
import com.order.application.dto.OrderResponse;
import com.order.application.dto.Response;
import com.order.application.query.GetAllOrdersQuery;
import com.order.application.query.GetOrderByIdQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Order Management.
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management APIs")
public class OrderController {

    private final CreateOrderCommand createOrderCommand;
    private final GetAllOrdersQuery getAllOrdersQuery;
    private final GetOrderByIdQuery getOrderByIdQuery;
    private final UpdatePaymentStatusCommand updatePaymentStatusCommand;

    /**
     * Lấy danh sách tất cả orders.
     *
     * @return danh sách orders
     */
    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve all orders")
    public Response<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> response = getAllOrdersQuery.execute();
        return Response.success(response);
    }

    /**
     * Lấy thông tin order theo ID.
     *
     * @param id ID của order
     * @return thông tin order
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by ID")
    public Response<OrderResponse> getOrderById(
            @PathVariable
            @Parameter(description = "Order ID")
            String id) {
        OrderResponse response = getOrderByIdQuery.execute(id);
        return Response.success(response);
    }

    /**
     * Tạo mới order.
     *
     * @param request thông tin order cần tạo
     * @return thông tin order vừa được tạo
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order")
    public Response<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // Current user ID will be extracted from JWT token in CreateOrderCommand
        String currentUserId = null; // Will be extracted from SecurityHelper
        OrderResponse response = createOrderCommand.execute(request, currentUserId);
        return Response.success("Order created successfully", response);
    }

    /**
     * Cập nhật trạng thái thanh toán cho order.
     * Callback endpoint từ payment-service.
     *
     * @param orderId ID của order
     * @param request request chứa thông tin cập nhật
     * @return response
     */
    @PatchMapping("/{orderId}/payment-status")
    @Operation(summary = "Update payment status", description = "Callback from payment service to update payment status")
    public Response<Void> updatePaymentStatus(
            @PathVariable
            @Parameter(description = "Order ID")
            String orderId,
            @RequestBody PaymentStatusUpdateRequest request) {
        updatePaymentStatusCommand.execute(orderId, request.getPaymentStatus(),
                request.getTransactionId(), request.getFailureReason());
        return Response.success("Payment status updated successfully");
    }

    /**
     * Request DTO để cập nhật trạng thái thanh toán.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentStatusUpdateRequest {
        private String paymentStatus; // PAID, FAILED
        private String transactionId;
        private String failureReason;
    }
}
