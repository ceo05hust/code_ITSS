package com.aims.payment_service.controller;

import com.aims.payment_service.dto.ApiResponse;
import com.aims.payment_service.dto.InvoiceRequest;
import com.aims.payment_service.entity.*;
import com.aims.payment_service.exception.*;
import com.aims.payment_service.repository.InvoiceRepository;
import com.aims.payment_service.repository.TransactionInfoRepository;
import com.aims.payment_service.subsystem.vietqr.IPaymentQRCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PayOrderController: REST API cho Angular frontend.
 *
 * Endpoints:
 *   POST /api/payment/generate-qr          — Tạo QR code thanh toán
 *   POST /api/payment/confirm              — Trigger test callback (simulate payment)
 *   POST /api/payment/switch-method        — Chuyển phương thức thanh toán
 *   GET  /api/payment/transaction/{id}     — Lấy thông tin giao dịch theo invoiceId
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayOrderController {

    private final IPaymentQRCode            paymentQRCode;
    private final InvoiceRepository          invoiceRepository;
    private final TransactionInfoRepository  transactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // =========================================================
    // 1. Generate QR Code
    // =========================================================

    @PostMapping("/generate-qr")
    public ResponseEntity<ApiResponse<Object>> generateQRCode(
            @Valid @RequestBody InvoiceRequest request
    ) {
        log.info("generateQRCode for invoice: {}", request.getInvoiceId());
        try {
            Invoice invoice = getOrCreateInvoice(request);

            QRCode qrCode = paymentQRCode.generateQRCode(invoice);
            log.info("QR generated successfully for invoice: {}", invoice.getInvoiceId());

            Map<String, Object> responseData = Map.of(
                    "qrCode", qrCode,
                    "invoiceId", invoice.getInvoiceId()
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
        Integer invoiceId = Integer.valueOf(body.get("invoiceId").toString());
        log.info("confirmPayment triggered for invoice: {}", invoiceId);

        try {
            // Kiểm tra invoice tồn tại
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new UnknownException("Invoice not found: " + invoiceId));

            // Kiểm tra đã thanh toán chưa (alt: already paid)
            if (invoice.getTransactionInfo() != null) {
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of("transactionId", invoice.getTransactionInfo().getTransactionId(),
                               "invoiceId", invoiceId, "status", "SUCCESS")
                ));
            }

            // Trigger VietQR test callback (bất đồng bộ — VietQR sẽ gọi ngược về callback endpoint)
            log.info("Triggering VietQR test callback for invoice: {}", invoiceId);
            checkPaymentStatus(invoice); // Bỏ qua kết quả trả về từ trigger API

            // Chờ 700ms để VietQR kịp gọi callback về và webhook xử lý xong
            Thread.sleep(700);

            // Xóa L1 cache để Hibernate buộc đọc lại từ DB (không dùng bản cache cũ)
            entityManager.clear();
            Invoice refreshed = invoiceRepository.findById(invoiceId).orElse(invoice);
            if (refreshed.getTransactionInfo() != null) {
                log.info("Invoice {} paid successfully via VietQR callback", invoiceId);
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of("transactionId", refreshed.getTransactionInfo().getTransactionId(),
                               "invoiceId", invoiceId, "status", "SUCCESS")
                ));
            }

            // Webhook chưa đến kịp (hiếm gặp) — thông báo pending
            log.warn("Invoice {} still pending after waiting for callback", invoiceId);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Payment pending - please wait",
                    Map.of("invoiceId", invoiceId, "status", "PENDING")
            ));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body(ApiResponse.error("Interrupted"));

        } catch (PaymentTimeoutException e) {
            log.warn("Payment timeout for invoice: {}", invoiceId);
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
        Integer invoiceId = Integer.valueOf(body.get("invoiceId").toString());
        log.info("switchMethod: invoice={}, method={}", invoiceId, method);

        if ("PayPal".equalsIgnoreCase(method)) {
            // ref: Pay Order by Credit Card — redirect to PayPal flow
            return ResponseEntity.ok(ApiResponse.ok(
                    "Switched to PayPal",
                    Map.of("method", "PayPal", "invoiceId", invoiceId)
            ));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                "Switched to " + method,
                Map.of("method", method, "invoiceId", invoiceId)
        ));
    }

    // =========================================================
    // 4. Get Transaction Result (Frontend polls this)
    // =========================================================

    @GetMapping("/transaction/{invoiceId}")
    public ResponseEntity<ApiResponse<TransactionInfo>> returnTransactionInfo(
            @PathVariable Integer invoiceId
    ) {
        log.info("getTransactionInfo for invoice: {}", invoiceId);

        return transactionRepository.findByInvoice_InvoiceId(invoiceId)
                .map(txn -> ResponseEntity.ok(ApiResponse.ok(txn)))
                .orElseGet(() -> ResponseEntity.ok(
                        ApiResponse.error("No transaction found for invoice: " + invoiceId)
                ));
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
    // Private: validate callback
    // =========================================================

    public boolean validateCallback(PaymentStatus paymentStatus) {
        return paymentStatus != null
                && paymentStatus.getStatus() != null
                && !paymentStatus.getStatus().isBlank();
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

    private void handlePaymentFailure(Exception e) {
        log.warn("[ALT] Payment Failed: {}", e.getMessage());
        // sendPaymentFailureMessage() — logged and propagated
    }

    // =========================================================
    // Helper: get or create Invoice
    // =========================================================

    private Invoice getOrCreateInvoice(InvoiceRequest request) {
        return invoiceRepository.findById(request.getInvoiceId())
                .orElseGet(() -> {
                    Invoice inv = Invoice.builder()
                            .shippingFee(request.getShippingFee())
                            .totalProductPriceExVAT(request.getTotalProductPriceExVAT())
                            .totalProductPriceIncVAT(request.getTotalProductPriceIncVAT())
                            .totalAmount(request.getTotalAmount())
                            .build();
                    return invoiceRepository.save(inv);
                });
    }
}
