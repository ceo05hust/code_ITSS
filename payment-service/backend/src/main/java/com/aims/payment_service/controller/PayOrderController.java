package com.aims.payment_service.controller;

import com.aims.payment_service.dto.ApiResponse;
import com.aims.payment_service.dto.InvoiceRequest;
import com.aims.payment_service.entity.*;
import com.aims.payment_service.exception.*;
import com.aims.payment_service.service.PaymentCacheService;
import com.aims.payment_service.subsystem.vietqr.IPaymentQRCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * PayOrderController: REST API cho Angular frontend.
 *
 * Endpoints:
 *   POST /api/payment/generate-qr          — Tạo QR code thanh toán (Invoice lưu tạm vào Memory)
 *   POST /api/payment/confirm              — Trigger test callback, kiểm tra thanh toán
 *   POST /api/payment/switch-method        — Chuyển phương thức thanh toán
 *   GET  /api/payment/transaction/{ref}    — Lấy thông tin giao dịch theo paymentRef
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayOrderController {

    private final IPaymentQRCode     paymentQRCode;
    private final PaymentCacheService paymentCacheService;


    // =========================================================
    // 1. Generate QR Code
    // =========================================================

    @PostMapping("/generate-qr")
    public ResponseEntity<ApiResponse<Object>> generateQRCode(
            @Valid @RequestBody InvoiceRequest request
    ) {
        log.info("generateQRCode for request: {}", request);
        try {
            // Tạo paymentRef duy nhất
            String paymentRef = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Tạo Invoice tạm (chưa lưu DB)
            Invoice invoice = Invoice.builder()
                    .shippingFee(request.getShippingFee())
                    .totalProductPriceExVAT(request.getTotalProductPriceExVAT())
                    .totalProductPriceIncVAT(request.getTotalProductPriceIncVAT())
                    .totalAmount(request.getTotalAmount())
                    .vietQrTransactionId(paymentRef) // Lưu tạm paymentRef vào đây để IPaymentQRCode lấy làm content
                    .build();

            // Lưu vào memory cache
            paymentCacheService.addPendingInvoice(paymentRef, invoice);

            QRCode qrCode = paymentQRCode.generateQRCode(invoice);
            log.info("QR generated successfully for paymentRef: {}", paymentRef);

            Map<String, Object> responseData = Map.of(
                    "qrCode", qrCode,
                    "paymentRef", paymentRef
            );

            return ResponseEntity.ok(ApiResponse.ok("QR code generated", responseData));

        } catch (InvalidTokenException e) {
            log.error("Auth failure while generating QR: {}", e.getMessage());
            handleAuthenticationFailure(e);
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("Service unavailable: " + e.getMessage()));

        } catch (QRCodeGenerationException e) {
            log.error("QR generation failed: {}", e.getMessage());
            handleQRGenerationFailure(e);
            return ResponseEntity.status(502)
                    .body(ApiResponse.error("QR generation failed: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Unknown error generating QR: {}", e.getMessage());
            throw new UnknownException("Unexpected error: " + e.getMessage());
        }
    }

    // =========================================================
    // 2. Confirm Payment (Trigger Test Callback)
    // =========================================================

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Object>> confirmPayment(
            @RequestBody Map<String, Object> body
    ) {
        String paymentRef = body.get("paymentRef").toString();
        log.info("confirmPayment triggered for paymentRef: {}", paymentRef);

        try {
            // Kiểm tra cache hoàn tất trước
            TransactionInfo completed = paymentCacheService.getCompletedPayment(paymentRef);
            if (completed != null) {
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of("transactionId", completed.getTransactionId(),
                               "paymentRef", paymentRef, "status", "SUCCESS")
                ));
            }

            // Nếu chưa hoàn tất, kiểm tra pending
            Invoice pendingInvoice = paymentCacheService.getPendingInvoice(paymentRef);
            if (pendingInvoice == null) {
                throw new UnknownException("Payment session not found or expired: " + paymentRef);
            }

            // Trigger VietQR test callback (bất đồng bộ)
            log.info("Triggering VietQR test callback for paymentRef: {}", paymentRef);
            checkPaymentStatus(pendingInvoice); 

            // Chờ 700ms để VietQR kịp gọi callback về và webhook xử lý xong
            Thread.sleep(700);

            // Kiểm tra lại cache xem webhook đã chạy chưa
            TransactionInfo refreshed = paymentCacheService.getCompletedPayment(paymentRef);
            if (refreshed != null) {
                log.info("Payment {} paid successfully via VietQR callback", paymentRef);
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of("transactionId", refreshed.getTransactionId(),
                               "paymentRef", paymentRef, "status", "SUCCESS")
                ));
            }

            // Webhook chưa đến kịp
            log.warn("Payment {} still pending after waiting for callback", paymentRef);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Payment pending - please wait",
                    Map.of("paymentRef", paymentRef, "status", "PENDING")
            ));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body(ApiResponse.error("Interrupted"));

        } catch (PaymentTimeoutException e) {
            log.warn("Payment timeout for paymentRef: {}", paymentRef);
            handleTimeout(e);
            return ResponseEntity.status(408)
                    .body(ApiResponse.error("Payment timeout: " + e.getMessage()));

        } catch (CallbackValidationException e) {
            log.error("Callback validation failed: {}", e.getMessage());
            handleCallbackViolation(e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("Security verification failed: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }


    // =========================================================
    // 3. Switch Payment Method
    // =========================================================

    @PostMapping("/switch-method")
    public ResponseEntity<ApiResponse<Object>> switchMethod(
            @RequestBody Map<String, Object> body
    ) {
        String method    = body.get("method").toString();
        String paymentRef = body.get("paymentRef").toString();
        log.info("switchMethod: paymentRef={}, method={}", paymentRef, method);

        if ("PayPal".equalsIgnoreCase(method)) {
            return ResponseEntity.ok(ApiResponse.ok(
                    "Switched to PayPal",
                    Map.of("method", "PayPal", "paymentRef", paymentRef)
            ));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                "Switched to " + method,
                Map.of("method", method, "paymentRef", paymentRef)
        ));
    }

    // =========================================================
    // 4. Get Transaction Result (Frontend polls this)
    // =========================================================

    @GetMapping("/transaction/{paymentRef}")
    public ResponseEntity<ApiResponse<TransactionInfo>> returnTransactionInfo(
            @PathVariable String paymentRef
    ) {
        log.info("getTransactionInfo for paymentRef: {}", paymentRef);

        TransactionInfo txn = paymentCacheService.getCompletedPayment(paymentRef);
        if (txn != null) {
            return ResponseEntity.ok(ApiResponse.ok(txn));
        }
        return ResponseEntity.ok(ApiResponse.error("No transaction found for paymentRef: " + paymentRef));
    }

    // =========================================================
    // Helper: check payment status (gọi VietQR test API)
    // =========================================================

    public PaymentStatus checkPaymentStatus(Invoice invoice) {
        try {
            String result = paymentQRCode.checkPaymentStatus(invoice);
            return new PaymentStatus(
                    "SUCCESS".equals(result) ? "00" : result,
                    "Payment status: " + result
            );
        } catch (Exception e) {
            return new PaymentStatus("FAILED", e.getMessage());
        }
    }


    // =========================================================
    // Private: alternative flow handlers (theo class diagram)
    // =========================================================

    private void handleAuthenticationFailure(Exception e) {
        log.error("[ALT] Authentication Failure: {}", e.getMessage());
        // sendServiceUnavailableError() — logged and propagated
    }

    private void handleQRGenerationFailure(Exception e) {
        log.error("[ALT] QR Generation Failure: {}", e.getMessage());
        // sendPaymentGenerationError() — logged and propagated
    }

    private void handleTimeout(Exception e) {
        log.warn("[ALT] Payment Timeout: {}", e.getMessage());
        // sendQRCodeExpired() — logged and propagated
    }

    private void handleCallbackViolation(Exception e) {
        log.error("[ALT] Callback Violation (Data Tampered): {}", e.getMessage());
        // sendInvalid() — logged and propagated
    }

}
