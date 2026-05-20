package com.payment.interfaces.rest;

import com.payment.application.command.ProcessPaymentCommand;
import com.payment.application.dto.PaymentResponse;
import com.payment.application.dto.ProcessPaymentRequest;
import com.payment.application.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Payment Management.
 */
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing APIs")
public class PaymentController {

    private final ProcessPaymentCommand processPaymentCommand;

    /**
     * Xử lý thanh toán cho một đơn hàng.
     *
     * @param request thông tin thanh toán
     * @return kết quả xử lý thanh toán
     */
    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Process payment for an order")
    public Response<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = processPaymentCommand.execute(request);
        return Response.success("Payment processed", response);
    }
}
