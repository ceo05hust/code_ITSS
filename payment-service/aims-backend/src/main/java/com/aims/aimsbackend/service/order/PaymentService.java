package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.dto.SimulatePaymentRequest;
import com.aims.aimsbackend.dto.WebhookRequest;
import com.aims.aimsbackend.entity.enums.PaymentMethod;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.subsystem.vietqr.IPaymentQRCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IPaymentQRCode vietQRService;
    private final TransactionInfoService transactionInfoService;

    @Value("${vietqr.callback.url:http://localhost:8080/api/payment/webhook}")
    private String webhookCallbackUrl;

    // =========================================================
    // 1. Generate VietQR Code
    // =========================================================

    public GenerateQRResult generateVietQR(InvoiceResponse invoiceResponse) {
        String externalTransactionId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Generating QR code — externalTransactionId={}", externalTransactionId);

        QRCode qrCode = vietQRService.generateQRCode(invoiceResponse, externalTransactionId);

        log.info("QR generated successfully — externalTransactionId={}", externalTransactionId);
        return new GenerateQRResult(externalTransactionId, qrCode);
    }

    // =========================================================
    // 2. Simulate Payment
    // =========================================================

    public void simulatePayment(SimulatePaymentRequest request) {
        log.info("Simulating payment — externalTransactionId={}, amount={}",
                request.getExternalTransactionId(), request.getAmount());

        vietQRService.triggerTestCallback(
                request.getExternalTransactionId(),
                request.getAmount()
        );

        log.info("Simulate payment request sent to VietQR");
    }

    // =========================================================
    // 3. Process Webhook from VietQR
    // =========================================================

    public TransactionInfo processWebhook(WebhookRequest webhookRequest) {
        log.info("Processing VietQR webhook — transactionRefId={}, amount={}",
                webhookRequest.getTransactionRefId(), webhookRequest.getAmount());

        String extractedId = webhookRequest.extractTransactionRefId();
        if (extractedId == null) {
            log.error("Could not extract externalTransactionId from webhook request");
            throw new IllegalArgumentException("Missing or invalid transaction reference in webhook");
        }

        TransactionInfo existingTxn = transactionInfoService.findByExternalTransactionId(extractedId);
        if (existingTxn != null) {
            log.warn("Transaction {} has already been paid.", extractedId);
            throw new com.aims.aimsbackend.exception.order.PaymentFailedException("Transaction has already been paid.");
        }

        String content = (webhookRequest.getContent() != null && !webhookRequest.getContent().isBlank())
                ? webhookRequest.getContent()
                : "AIMS " + extractedId;

        TransactionInfo saved = transactionInfoService.createTransactionInfo(
                extractedId,
                content,
                webhookRequest.getAmount(),
                PaymentMethod.VIETQR
        );

        log.info("Transaction saved to DB — transactionId={}, externalTransactionId={}",
                saved.getTransactionId(), saved.getExternalTransactionId());

        return saved;
    }

    // =========================================================
    
    // =========================================================

    public record GenerateQRResult(String externalTransactionId, QRCode qrCode) {}
}
