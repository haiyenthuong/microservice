package com.payment.interfaces.rest;

import com.payment.application.dto.*;
import com.payment.application.service.VnpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller cho Payment Management.
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing APIs")
public class PaymentController {

    private final VnpayService vnpayService;

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

    // ==================== VNPAY Endpoints ====================

    /**
     * Tạo URL thanh toán VNPAY cho đơn hàng.
     *
     * @param request Thông tin tạo thanh toán
     * @return URL thanh toán VNPAY
     */
    @PostMapping("/vnpay/create-url")
    @Operation(summary = "Create VNPAY payment URL", description = "Tạo URL thanh toán VNPAY cho đơn hàng")
    public Response<CreatePaymentUrlResponse> createVnpayPaymentUrl(
            @RequestBody CreatePaymentUrlRequest request) {
        log.info("Creating VNPAY payment URL for order: {}", request.getOrderId());

        try {
            CreatePaymentUrlResponse response = vnpayService.createPaymentUrl(request);
            return Response.success("Payment URL created successfully", response);
        } catch (Exception e) {
            log.error("Error creating VNPAY payment URL: {}", e.getMessage(), e);
            return Response.error(500, "Failed to create payment URL: " + e.getMessage());
        }
    }

    /**
     * Xử lý IPN callback từ VNPAY.
     * VNPAY sẽ gọi endpoint này để notify về trạng thái thanh toán.
     *
     * @param request HttpServletRequest chứa IPN data
     * @return IpnResponse theo format VNPAY yêu cầu
     */
    @PostMapping("/vnpay/ipn")
    @Operation(summary = "VNPAY IPN callback", description = "Xử lý IPN callback từ VNPAY")
    public IpnResponse handleVnpayIpn(HttpServletRequest request) {
        log.info("Received VNPAY IPN callback");

        try {
            Map<String, String> ipnData = extractRequestData(request);
            log.info("VNPAY IPN Data: {}", ipnData);

            return vnpayService.processIpn(ipnData);
        } catch (Exception e) {
            log.error("Error processing VNPAY IPN: {}", e.getMessage(), e);
            return IpnResponse.error("IPN processing failed: " + e.getMessage());
        }
    }

    /**
     * Xử lý return URL sau khi thanh toán.
     * Sau khi người dùng hoàn thành thanh toán trên VNPAY, họ sẽ được redirect về URL này.
     *
     * @param request HttpServletRequest chứa return data
     * @return Kết quả xử lý và redirect
     */
    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPAY return URL handler", description = "Xử lý sau khi người dùng hoàn thành thanh toán")
    public Response<Map<String, Object>> handleVnpayReturn(HttpServletRequest request) {
        log.info("Received VNPAY return callback");

        try {
            Map<String, String> returnData = extractRequestData(request);
            log.info("VNPAY Return Data: {}", returnData);

            // Extract order code từ vnp_TxnRef
            String orderCode = returnData.get("vnp_TxnRef");
            String responseCode = returnData.get("vnp_ResponseCode");

            Map<String, Object> result = new HashMap<>();
            result.put("orderCode", orderCode);
            result.put("responseCode", responseCode);
            result.put("success", "00".equals(responseCode));
            result.put("message", "00".equals(responseCode)
                    ? "Thanh toán thành công!"
                    : "Thanh toán thất bại. Mã lỗi: " + responseCode);

            // Query transaction status
            if (orderCode != null) {
                try {
                    TransactionStatusResponse status = vnpayService.queryTransactionStatus(orderCode);
                    result.put("transactionStatus", status);
                } catch (Exception e) {
                    log.warn("Failed to query transaction status: {}", e.getMessage());
                }
            }

            return Response.success("Payment result processed", result);

        } catch (Exception e) {
            log.error("Error processing VNPAY return: {}", e.getMessage(), e);
            return Response.error(500, "Failed to process return: " + e.getMessage());
        }
    }

    /**
     * Query trạng thái giao dịch theo order code.
     *
     * @param orderCode Mã đơn hàng
     * @return Thông tin trạng thái giao dịch
     */
    @GetMapping("/vnpay/query/{orderCode}")
    @Operation(summary = "Query VNPAY transaction status", description = "Truy vấn trạng thái giao dịch VNPAY")
    public Response<TransactionStatusResponse> queryVnpayTransaction(
            @PathVariable @Parameter(description = "Mã đơn hàng") String orderCode) {
        log.info("Querying VNPAY transaction for order code: {}", orderCode);

        try {
            TransactionStatusResponse response = vnpayService.queryTransactionStatus(orderCode);
            return Response.success("Transaction status retrieved", response);
        } catch (IllegalArgumentException e) {
            log.warn("Transaction not found: {}", e.getMessage());
            return Response.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("Error querying transaction: {}", e.getMessage(), e);
            return Response.error(500, "Failed to query transaction: " + e.getMessage());
        }
    }

    /**
     * Extract request data từ HttpServletRequest thành Map.
     * Dùng cho cả IPN và return URL handling.
     */
    private Map<String, String> extractRequestData(HttpServletRequest request) {
        Map<String, String> data = new HashMap<>();

        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            data.put(paramName, paramValue);
        }

        return data;
    }

    // ==================== Legacy Endpoints ====================

    /**
     * Lấy thông tin payment theo ID.
     *
     * @param id Payment ID
     * @return thông tin payment
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment information by ID")
    public Response<String> getPaymentById(
            @PathVariable @Parameter(description = "Payment ID") String id) {
        // TODO: Implement get payment by ID
        return Response.success("Payment retrieval not yet implemented via REST. Use Kafka events.");
    }
}
