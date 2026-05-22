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

/**
 * PaymentService — Business Logic Layer cho use-case PayOrder by VietQR.
 *
 * Chịu trách nhiệm:
 *   1. generateVietQR      — Tạo mã externalTransactionId và gọi subsystem sinh mã QR
 *   2. simulatePayment     — Gọi subsystem kích hoạt giả lập thanh toán
 *   3. processWebhook      — Xử lý callback từ VietQR và lưu giao dịch vào DB
 */
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

    /**
     * Tạo mã externalTransactionId duy nhất và gọi VietQRController để sinh mã QR.
     *
     * @param invoiceResponse Thông tin đơn hàng từ Client (tổng tiền, phí ship, v.v.)
     * @return GenerateQRResult chứa externalTransactionId và QRCode
     */
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

    /**
     * Kích hoạt giả lập thanh toán thành công.
     * Gọi VietQRController để POST tới VietQR test-callback API.
     * VietQR sẽ gọi lại webhook URL của chúng ta sau vài giây.
     *
     * @param request Yêu cầu giả lập chứa externalTransactionId và amount
     */
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

    /**
     * Xử lý webhook callback từ VietQR sau khi giao dịch thanh toán thành công.
     * Lưu thông tin giao dịch vào bảng transaction_info trong DB.
     *
     * @param webhookRequest Payload từ VietQR chứa thông tin giao dịch
     * @return TransactionInfo đối tượng đã được lưu vào DB
     */
    public TransactionInfo processWebhook(WebhookRequest webhookRequest) {
        log.info("Processing VietQR webhook — transactionRefId={}, amount={}",
                webhookRequest.getTransactionRefId(), webhookRequest.getAmount());

        String extractedId = webhookRequest.extractTransactionRefId();
        if (extractedId == null) {
            log.error("Could not extract externalTransactionId from webhook request");
            throw new IllegalArgumentException("Missing or invalid transaction reference in webhook");
        }

        // Chặn giao dịch trùng lặp: Nếu mã QR này đã thanh toán rồi thì từ chối webhook
        TransactionInfo existingTxn = transactionInfoService.findByExternalTransactionId(extractedId);
        if (existingTxn != null) {
            log.warn("Transaction {} has already been paid.", extractedId);
            throw new com.aims.aimsbackend.exception.order.PaymentFailedException("Transaction has already been paid.");
        }

        // Nội dung giao dịch: ưu tiên lấy từ webhook, nếu không có thì tự tổng hợp
        String content = (webhookRequest.getContent() != null && !webhookRequest.getContent().isBlank())
                ? webhookRequest.getContent()
                : "AIMS " + extractedId;

        // Lưu giao dịch vào DB thông qua TransactionInfoService
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
    // Inner Result DTO (tránh tạo thêm file riêng)
    // =========================================================

    /**
     * Kết quả trả về sau khi tạo QR thành công.
     */
    public record GenerateQRResult(String externalTransactionId, QRCode qrCode) {}
}
