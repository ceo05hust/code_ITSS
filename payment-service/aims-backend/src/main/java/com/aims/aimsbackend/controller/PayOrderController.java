package com.aims.aimsbackend.controller;

import com.aims.aimsbackend.dto.ApiResponse;
import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.dto.SimulatePaymentRequest;
import com.aims.aimsbackend.dto.WebhookRequest;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.exception.order.UnknownException;
import com.aims.aimsbackend.service.order.PaymentService;
import com.aims.aimsbackend.service.order.PaymentService.GenerateQRResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PayOrderController — REST API endpoints cho use-case PayOrder by VietQR.
 *
 * Endpoints:
 *   POST /api/payment/generate-qr       — Nhận thông tin đơn hàng, trả về QR code + externalTransactionId
 *   POST /api/payment/simulate-payment  — Kích hoạt giả lập thanh toán thành công (chỉ dùng khi test)
 *   POST /api/payment/webhook           — Nhận callback từ VietQR sau khi thanh toán thành công
 *
 * Test bằng Postman:
 *   1. POST /api/payment/generate-qr        Body: { "shippingFee": 12000, "totalAmount": 507000, ... }
 *   2. POST /api/payment/simulate-payment   Body: { "externalTransactionId": "REF-XXXXXX", "amount": 507000 }
 *   3. Webhook sẽ tự động được gọi bởi VietQR — không cần gọi thủ công
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayOrderController {

    private final PaymentService paymentService;
    private final com.aims.aimsbackend.service.order.TransactionInfoService transactionInfoService;

    // =========================================================
    // POST /api/payment/generate-qr
    // =========================================================

    @PostMapping("/generate-qr")
    public ResponseEntity<ApiResponse<Object>> generateQRCode(
            @Valid @RequestBody InvoiceResponse response
    ) {
        log.info("generateQRCode called — totalAmount={}", response.getTotalAmount());
        try {
            GenerateQRResult result = paymentService.generateVietQR(response);

            Map<String, Object> responseData = Map.of(
                    "externalTransactionId", result.externalTransactionId(),
                    "qrCode",               result.qrCode()
            );
            return ResponseEntity.ok(ApiResponse.ok("QR code generated successfully", responseData));

        } catch (InvalidTokenException e) {
            log.error("[AUTH FAILURE] {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("VietQR auth failed: " + e.getMessage()));

        } catch (QRCodeGenerationException e) {
            log.error("[QR FAILURE] {}", e.getMessage());
            return ResponseEntity.status(502)
                    .body(ApiResponse.error("QR generation failed: " + e.getMessage()));

        } catch (Exception e) {
            log.error("[UNKNOWN] {}", e.getMessage());
            throw new UnknownException("Unexpected error: " + e.getMessage());
        }
    }

    // =========================================================
    // POST /api/payment/simulate-payment
    // =========================================================

    /**
     * Kích hoạt giả lập thanh toán thành công.
     * Chỉ sử dụng trong môi trường test/dev — KHÔNG dùng trên production.
     */
    @PostMapping("/simulate-payment")
    public ResponseEntity<ApiResponse<Object>> simulatePayment(
            @Valid @RequestBody SimulatePaymentRequest request
    ) {
        log.info("simulatePayment called — externalTransactionId={}", request.getExternalTransactionId());
        try {
            paymentService.simulatePayment(request);

            return ResponseEntity.ok(ApiResponse.ok(
                    "Simulate payment request sent. VietQR will call back to our webhook shortly.",
                    Map.of("externalTransactionId", request.getExternalTransactionId())
            ));

        } catch (InvalidTokenException e) {
            log.error("[AUTH FAILURE] {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("VietQR auth failed: " + e.getMessage()));

        } catch (Exception e) {
            log.error("[SIMULATE FAILURE] {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Simulation failed: " + e.getMessage()));
        }
    }

    // =========================================================
    // GET /api/payment/transaction/{externalTransactionId}
    // =========================================================

    /**
     * API Polling để Frontend kiểm tra trạng thái giao dịch.
     * Trả về SUCCESS nếu webhook đã lưu, ngược lại trả về PENDING.
     */
    @GetMapping("/transaction/{externalTransactionId}")
    public ResponseEntity<ApiResponse<Object>> getTransactionStatus(
            @PathVariable String externalTransactionId
    ) {
        log.info("getTransactionStatus called — externalTransactionId={}", externalTransactionId);
        try {
            TransactionInfo txn = transactionInfoService.findByExternalTransactionId(externalTransactionId);
            if (txn != null) {
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of(
                                "status", "SUCCESS",
                                "transactionId", txn.getTransactionId(),
                                "externalTransactionId", txn.getExternalTransactionId(),
                                "amount", txn.getAmount()
                        )
                ));
            }
            return ResponseEntity.ok(ApiResponse.ok(
                    "Payment pending",
                    Map.of("status", "PENDING", "externalTransactionId", externalTransactionId)
            ));
        } catch (Exception e) {
            log.error("[POLLING FAILURE] {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to check status: " + e.getMessage()));
        }
    }

    // =========================================================
    // POST /api/payment/switch-method
    // =========================================================

    /**
     * Chuyển đổi phương thức thanh toán sang PayPal hoặc method khác.
     */
    @PostMapping("/switch-method")
    public ResponseEntity<ApiResponse<Object>> switchMethod(
            @RequestBody Map<String, Object> body
    ) {
        String method = body.getOrDefault("method", "PayPal").toString();
        String externalTransactionId = body.getOrDefault("externalTransactionId", "").toString();
        log.info("switchMethod called — externalTransactionId={}, method={}", externalTransactionId, method);

        // Chúng ta chỉ trả về 200 OK để Frontend chuyển hướng sang luồng PayPal.
        // Có thể mở rộng để cập nhật trạng thái đơn hàng bị hủy bỏ vào Database nếu cần.
        return ResponseEntity.ok(ApiResponse.ok(
                "Switched to " + method,
                Map.of("method", method, "externalTransactionId", externalTransactionId)
        ));
    }
}
