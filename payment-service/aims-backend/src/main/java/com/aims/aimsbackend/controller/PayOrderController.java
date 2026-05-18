package com.aims.aimsbackend.controller;

import com.aims.aimsbackend.dto.ApiResponse;
import com.aims.aimsbackend.dto.InvoiceRequest;
import com.aims.aimsbackend.entity.PaymentStatus;
import com.aims.aimsbackend.entity.QRCode;
import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.exception.*;
import com.aims.aimsbackend.repository.order.TransactionInfoRepository;
import com.aims.aimsbackend.subsystem.vietqr.IPaymentQRCode;
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
 * Workflow đúng chuẩn nhóm: 
 * 1. Tạo QR Code (Invoice chưa được lưu vào DB).
 * 2. Thanh toán thành công -> VietQR gọi Webhook -> Lưu TransactionInfo vào DB.
 * 3. Frontend poll trạng thái -> Trả về TransactionInfo.
 * 4. Frontend gửi TransactionInfo đó cho PlaceOrderService để lưu Invoice và Order.
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayOrderController {

    private final IPaymentQRCode            paymentQRCode;
    private final TransactionInfoRepository transactionInfoRepository;

    // =========================================================
    // 1. Generate QR Code
    // =========================================================

    @PostMapping("/generate-qr")
    public ResponseEntity<ApiResponse<Object>> generateQRCode(
            @Valid @RequestBody InvoiceRequest request
    ) {
        log.info("generateQRCode for request: {}", request);
        try {
            // Tạo paymentRef ngẫu nhiên duy nhất vì Invoice chưa tồn tại trong DB
            String paymentRef = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Tạo một Invoice tạm thời trong bộ nhớ chỉ để truyền dữ liệu cho IPaymentQRCode
            Invoice tempInvoice = Invoice.builder()
                    .shippingFee(request.getShippingFee())
                    .totalProductPriceExclVAT(request.getTotalProductPriceExclVAT())
                    .totalProductPriceInclVAT(request.getTotalProductPriceInclVAT())
                    .totalAmount(request.getTotalAmount())
                    .paymentReference(paymentRef) 
                    .build();

            QRCode qrCode = paymentQRCode.generateQRCode(tempInvoice);
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
            // Kiểm tra xem TransactionInfo đã được tạo trong DB chưa (do VietQR Webhook gọi)
            TransactionInfo txn = transactionInfoRepository.findByTransactionContentContaining(paymentRef).orElse(null);

            if (txn != null) {
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of("transactionId", txn.getTransactionId(),
                               "paymentRef", paymentRef, "status", "SUCCESS")
                ));
            }

            // Nếu chưa, trigger VietQR test callback bằng cách tạo giả lập Invoice tạm
            // Note: Cần gửi kèm amount để test callback biết truyền vào amount nào (trong thực tế thì bỏ qua)
            Invoice tempInvoice = new Invoice();
            tempInvoice.setPaymentReference(paymentRef);
            if (body.containsKey("amount")) {
                tempInvoice.setTotalAmount(new java.math.BigDecimal(body.get("amount").toString()));
            } else {
                tempInvoice.setTotalAmount(new java.math.BigDecimal("10000")); // Fallback for test
            }

            log.info("Triggering VietQR test callback for paymentRef: {}", paymentRef);
            checkPaymentStatus(tempInvoice);

            // Chờ 700ms để VietQR kịp gọi callback về và webhook xử lý xong
            Thread.sleep(700);

            // Kiểm tra lại DB xem webhook đã tạo TransactionInfo chưa
            TransactionInfo refreshedTxn = transactionInfoRepository.findByTransactionContentContaining(paymentRef).orElse(null);
            
            if (refreshedTxn != null) {
                log.info("Payment {} paid successfully via VietQR callback", paymentRef);
                return ResponseEntity.ok(ApiResponse.ok(
                        "Payment successful",
                        Map.of("transactionId", refreshedTxn.getTransactionId(),
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
        String method     = body.get("method").toString();
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

        TransactionInfo txn = transactionInfoRepository.findByTransactionContentContaining(paymentRef).orElse(null);
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
    }

    private void handleQRGenerationFailure(Exception e) {
        log.error("[ALT] QR Generation Failure: {}", e.getMessage());
    }

    private void handleTimeout(Exception e) {
        log.warn("[ALT] Payment Timeout: {}", e.getMessage());
    }

    private void handleCallbackViolation(Exception e) {
        log.error("[ALT] Callback Violation (Data Tampered): {}", e.getMessage());
    }

}
