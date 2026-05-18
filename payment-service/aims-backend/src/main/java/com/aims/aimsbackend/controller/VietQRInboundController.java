package com.aims.aimsbackend.controller;

import com.aims.aimsbackend.dto.CallbackPayload;
import com.aims.aimsbackend.entity.enums.PaymentMethod;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.exception.CallbackValidationException;
import com.aims.aimsbackend.exception.PaymentFailedException;
import com.aims.aimsbackend.repository.order.TransactionInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * VietQRInboundController: xử lý các request từ phía VietQR gọi vào server của chúng ta.
 *
 * Workflow đúng chuẩn nhóm:
 * 1. Nhận webhook từ VietQR.
 * 2. Tạo và lưu TransactionInfo vào DB (Invoice lúc này chưa tồn tại, trường invoice = null).
 * 3. Frontend poll trạng thái thanh toán, nhận về TransactionInfo.
 * 4. Frontend tiếp tục gọi PlaceOrderService, lúc đó Invoice mới được tạo và gán với TransactionInfo này.
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

    private final TransactionInfoRepository  transactionRepository;

    // =========================================================
    // 1. Token endpoint — VietQR gọi để lấy Bearer token
    // =========================================================

    @PostMapping("/vqr/api/token_generate")
    public ResponseEntity<?> getToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("VietQR requesting token. Auth header: {}", authHeader);

        if (!isValidBasicAuth(authHeader)) {
            log.warn("Invalid Basic Auth for token request");
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid credentials"));
        }

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
            log.warn("VietQR callback: payment FAILED for transaction {}", payload.getTransactionRefId());
            throw new PaymentFailedException(
                    "Payment failed for transaction: " + payload.getTransactionRefId()
                    + ". Reason: " + payload.getMessage()
            );
        }

        // 3. Trích xuất paymentRef từ nội dung chuyển khoản ("AIMS REF-123456")
        String paymentRef = payload.extractInvoiceIdFromContent();
        log.info("Resolved paymentRef from callback: {}", paymentRef);

        if (paymentRef == null || paymentRef.isBlank()) {
            throw new PaymentFailedException("Could not extract payment reference from transfer content");
        }

        // 4. Tạo TransactionInfo (Vì Invoice chưa tồn tại nên truyền null cho invoice)
        String externalTxnId = payload.getTransactionId() != null
                        ? payload.getTransactionId()
                        : "TXN-" + UUID.randomUUID();

        String content = payload.getContent() != null
                        ? payload.getContent()
                        : "Thanh toan AIMS " + paymentRef;

        BigDecimal amount = BigDecimal.valueOf(payload.getAmount());

        TransactionInfo transactionInfo = TransactionInfo.createTransactionInfo(
                externalTxnId,
                content,
                null, // Invoice chưa tồn tại!
                amount,
                PaymentMethod.VIETQR
        );

        // 5. Lưu TransactionInfo vào DB để frontend có thể tìm thấy khi poll API
        transactionInfo = transactionRepository.save(transactionInfo);
        log.info("TransactionInfo saved: externalTxnId={}, internalId={}, linked to paymentRef={}", 
                 externalTxnId, transactionInfo.getTransactionId(), paymentRef);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Callback processed successfully",
                "transactionId", transactionInfo.getTransactionId()
        ));
    }

    // =========================================================
    // Helper methods
    // =========================================================

    private boolean isValidBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) return false;
        try {
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            String[] parts = decoded.split(":", 2);
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
