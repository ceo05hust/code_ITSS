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

    @PostMapping("/switch-method")
    public ResponseEntity<ApiResponse<Object>> switchMethod(
            @RequestBody Map<String, Object> body
    ) {
        String method = body.getOrDefault("method", "PayPal").toString();
        String externalTransactionId = body.getOrDefault("externalTransactionId", "").toString();
        log.info("switchMethod called — externalTransactionId={}, method={}", externalTransactionId, method);

        return ResponseEntity.ok(ApiResponse.ok(
                "Switched to " + method,
                Map.of("method", method, "externalTransactionId", externalTransactionId)
        ));
    }
}
