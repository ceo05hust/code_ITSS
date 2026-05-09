package com.aims.payment_service.controller;

import com.aims.payment_service.dto.CallbackPayload;
import com.aims.payment_service.entity.Invoice;
import com.aims.payment_service.entity.PaymentMethod;
import com.aims.payment_service.entity.TransactionInfo;
import com.aims.payment_service.exception.CallbackValidationException;
import com.aims.payment_service.exception.PaymentFailedException;
import com.aims.payment_service.repository.InvoiceRepository;
import com.aims.payment_service.repository.TransactionInfoRepository;
import com.aims.payment_service.service.PaymentCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * VietQRInboundController: xử lý các request từ phía VietQR gọi vào server của chúng ta.
 *
 * Endpoints:
 *   POST /vqr/bank/api/token_generate          — VietQR lấy token (Basic Auth: admin/admin)
 *   POST /vqr/bank/api/test/transaction-callback — VietQR gửi kết quả thanh toán
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class VietQRInboundController {

    @Value("${vietqr.callback.username}")
    private String callbackUsername;

    @Value("${vietqr.callback.password}")
    private String callbackPassword;

    @Value("${vietqr.callback.token}")
    private String callbackToken;

    private final InvoiceRepository     invoiceRepository;
    private final TransactionInfoRepository transactionRepository;
    private final PaymentCacheService    paymentCacheService;

    // =========================================================
    // 1. Token endpoint — VietQR gọi để lấy Bearer token
    // =========================================================

    /**
     * VietQR gọi endpoint này với Basic Auth (admin:admin) để lấy token.
     * Token này VietQR sẽ dùng trong header khi gọi callback của chúng ta.
     *
     * URL: POST /vqr/api/token_generate
     */
    @PostMapping("/vqr/api/token_generate")
    public ResponseEntity<?> getToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("VietQR requesting token. Auth header: {}", authHeader);

        // Validate Basic Auth
        if (!isValidBasicAuth(authHeader)) {
            log.warn("Invalid Basic Auth for token request");
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid credentials"));
        }

        // Trả về token (lần trước đã dùng dummy_token_123)
        Map<String, Object> tokenResponse = Map.of(
                "access_token", callbackToken,
                "token_type",   "bearer",
                "expires_in",   3600
        );

        log.info("Token issued to VietQR: {}", callbackToken);
        return ResponseEntity.ok(tokenResponse);
    }

    // =========================================================
    // 2. Callback endpoint — VietQR gửi kết quả thanh toán
    // =========================================================

    /**
     * VietQR gọi endpoint này khi có kết quả thanh toán (thật hoặc test).
     * Header: Authorization: Bearer dummy_token_123
     *
     * URLs hỗ trợ:
     *   POST /vqr/bank/api/test/transaction-callback
     *   POST /vqr/bank/api/transaction-sync
     */
    @PostMapping({"/vqr/bank/api/test/transaction-callback", "/vqr/bank/api/transaction-sync"})
    public ResponseEntity<?> receiveCallback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CallbackPayload payload
    ) {
        log.info("Received VietQR callback: transactionRefId={}, amount={}, status={}",
                payload.getTransactionRefId(), payload.getAmount(), payload.getStatus());

        // 1. Validate Bearer token (alt: Callback Data Tampered)
        if (!isValidBearerToken(authHeader)) {
            log.warn("Invalid Bearer token in callback. Possible data tampering.");
            throw new CallbackValidationException(
                    "Invalid bearer token in callback. Possible security violation."
            );
        }

        // 2. Kiểm tra payment status (alt: Simulated Payment Failed)
        if (!payload.isSuccess()) {
            log.warn("VietQR callback: payment FAILED for invoice {}", payload.getTransactionRefId());
            throw new PaymentFailedException(
                    "Payment failed for invoice: " + payload.getTransactionRefId()
                    + ". Reason: " + payload.getMessage()
            );
        }

        // 3. Trích xuất paymentRef từ nội dung chuyển khoản ("AIMS REF-XXXXXXXX")
        String paymentRef = payload.extractInvoiceIdFromContent();
        log.info("Resolved paymentRef from callback: {}", paymentRef);
        
        Invoice invoice = null;
        if (paymentRef != null) {
            invoice = paymentCacheService.getPendingInvoice(paymentRef);
        }

        if (invoice == null) {
            log.warn("Invoice not found in cache for paymentRef: {}", paymentRef);
            // Có thể payment đã xử lý rồi, hoặc QR cũ
            throw new PaymentFailedException("No pending payment session found for: " + paymentRef);
        }

        // 4. LƯU INVOICE VÀO DB (để lấy ID thật)
        invoice = invoiceRepository.save(invoice);
        log.info("Transient Invoice saved to DB with ID: {}", invoice.getInvoiceId());

        // 5. Tạo TransactionInfo
        String txnId = payload.getTransactionId() != null
                        ? payload.getTransactionId()
                        : "TXN-" + UUID.randomUUID();

        TransactionInfo transactionInfo = TransactionInfo.createTransactionInfo(
                txnId,
                payload.getContent() != null ? payload.getContent()
                        : "Thanh toan AIMS " + paymentRef,
                invoice,
                payload.getAmount(),
                PaymentMethod.VietQR
        );
        transactionRepository.save(transactionInfo);
        log.info("TransactionInfo saved: DB_ID generated, VietQR_TxnId={}", txnId);

        // 6. Cập nhật lại liên kết và Cache
        invoice.markAsPaid(transactionInfo);
        invoiceRepository.save(invoice);
        
        // Đưa vào Cache hoàn tất và xóa khỏi Cache chờ
        paymentCacheService.addCompletedPayment(paymentRef, transactionInfo);
        paymentCacheService.removePendingInvoice(paymentRef);
        log.info("Payment session {} completed successfully", paymentRef);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Callback processed successfully",
                "transactionId", txnId
        ));
    }

    // =========================================================
    // Helper methods
    // =========================================================

    private boolean isValidBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) return false;
        try {
            String decoded    = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            String[] parts    = decoded.split(":", 2);
            return parts.length == 2
                    && callbackUsername.equals(parts[0].trim())
                    && callbackPassword.equals(parts[1].trim());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7).trim();
        return callbackToken.equals(token);
    }
}
