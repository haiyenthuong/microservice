package com.payment.interfaces.rest;

import com.payment.application.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Payment Management.
 */
@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Management", description = "Payment processing APIs")
public class PaymentController {

    /**
     * Health check endpoint.
     *
     * @return status message
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if payment service is running")
    public Response<String> health() {
        return Response.success("Payment Service is running");
    }

    /**
     * Lấy thông tin payment theo ID.
     *
     * @param id Payment ID
     * @return thông tin payment
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment information by ID")
    public Response<String> getPaymentById(@PathVariable String id) {
        // TODO: Implement get payment by ID
        return Response.success("Payment retrieval not yet implemented via REST. Use Kafka events.");
    }
}
