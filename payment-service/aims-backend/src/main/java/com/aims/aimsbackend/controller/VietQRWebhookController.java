package com.aims.aimsbackend.controller;
import com.aims.aimsbackend.dto.ApiResponse;
import com.aims.aimsbackend.dto.WebhookRequest;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.service.order.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/vqr/bank/api")
@RequiredArgsConstructor
public class VietQRWebhookController {

    private final PaymentService paymentService;

    @PostMapping({"/transaction-sync", "/test/transaction-callback"})
    public ResponseEntity<Object> receiveWebhook(
            @RequestBody WebhookRequest webhookRequest
    ) {
        log.info("Webhook received from VietQR portal — transactionRefId={}, amount={}",
                webhookRequest.getTransactionRefId(), webhookRequest.getAmount());
        try {
            TransactionInfo saved = paymentService.processWebhook(webhookRequest);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Callback processed successfully",
                    "transactionId", saved.getTransactionId() != null ? saved.getTransactionId() : "TXN"
            ));
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()
            ));
        }
    }
}
