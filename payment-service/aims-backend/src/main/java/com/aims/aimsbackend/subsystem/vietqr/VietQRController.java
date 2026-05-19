package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.QRCode;
import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.subsystem.vietqr.request.QRAccessTokenRequest;
import com.aims.aimsbackend.subsystem.vietqr.request.QRGenerateRequest;
import com.aims.aimsbackend.subsystem.vietqr.response.QRAccessTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * VietQRController (Service layer):
 * Xử lý business logic cho VietQR subsystem.
 * Implements IPaymentQRCode, sử dụng VietQRBoundary để gọi API.
 */
@Slf4j
@Service
public class VietQRController implements IPaymentQRCode {

    private final VietQRBoundary boundary;

    @Value("${vietqr.api.username}")
    private String apiUsername;

    @Value("${vietqr.api.password}")
    private String apiPassword;

    @Value("${vietqr.bank.code}")
    private String bankCode;

    @Value("${vietqr.bank.name}")
    private String bankName;

    @Value("${vietqr.bank.account}")
    private String bankAccount;

    @Value("${vietqr.bank.user-name}")
    private String bankUserName;

    // Token cache
    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public VietQRController(VietQRBoundary boundary) {
        this.boundary = boundary;
    }

    /**
     * Lấy access token hợp lệ (có cache, tự refresh khi hết hạn).
     */
    public String getValidAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            log.debug("Using cached VietQR access token");
            return cachedToken;
        }

        log.info("Fetching new VietQR access token...");
        QRAccessTokenRequest tokenRequest = new QRAccessTokenRequest(apiUsername, apiPassword);
        String authHeader    = tokenRequest.buildAuthorizationHeader();
        String rawResponse   = boundary.getAccessToken(authHeader);

        QRAccessTokenResponse tokenResponse = new QRAccessTokenResponse();
        tokenResponse.parseResponseString(rawResponse);

        if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new InvalidTokenException("VietQR returned empty access token");
        }

        this.cachedToken = tokenResponse.getAccessToken();
        // Giảm 60 giây để an toàn
        this.tokenExpiry = Instant.now().plusSeconds(
                Math.max(tokenResponse.getExpiresIn() - 60, 60)
        );

        log.info("VietQR access token acquired, expires in {}s", tokenResponse.getExpiresIn());
        return cachedToken;
    }

    /**
     * Tạo QR Code thanh toán cho invoice.
     */
    @Override
    public QRCode generateQRCode(Invoice invoice) {
        String token = getValidAccessToken();

        QRGenerateRequest generateRequest = QRGenerateRequest.builder()
                .bankCode(bankCode)
                .bankName(bankName)
                .bankAccount(bankAccount)
                .userBankName(bankUserName)
                .amount(invoice.getTotalAmount())
                .content("AIMS " + invoice.getPaymentReference()) // paymentRef là duy nhất
                .qrType(0)
                .orderId(invoice.getPaymentReference())
                .transType("C")
                .build();

        String rawResponse = boundary.generateQRCode(generateRequest.buildRequestString(), token);

        QRCode qrCode = new QRCode();
        qrCode.parseQRCodeResponse(rawResponse);

        if (qrCode.getQrCode() == null || qrCode.getQrCode().isBlank()) {
            throw new QRCodeGenerationException("VietQR returned empty QR code for invoice " + invoice.getInvoiceId());
        }

        return qrCode;
    }

    /**
     * Gọi VietQR để trigger test callback (simulate payment).
     *
     * @param invoice Hóa đơn cần kiểm tra thanh toán giả lập
     * @return "SUCCESS" | "FAILED" | "PENDING"
     */
    @Override
    public String checkPaymentStatus(Invoice invoice) {
        String token = getValidAccessToken();

        // Body gửi đến VietQR test callback trigger
        String requestBody = String.format(
                "{\"bankAccount\":\"%s\",\"content\":\"AIMS %s\",\"amount\":%.0f,\"bankCode\":\"%s\",\"transType\":\"C\"}",
                bankAccount, invoice.getPaymentReference(), invoice.getTotalAmount(), bankCode
        );

        String rawResponse = boundary.checkPaymentStatus(requestBody, token);
        log.info("VietQR checkPaymentStatus raw response: {}", rawResponse);

        // Parse status từ response
        if (rawResponse.contains("\"00\"") || rawResponse.toLowerCase().contains("success")) {
            return "SUCCESS";
        } else if (rawResponse.contains("FAILED") || rawResponse.contains("failed")) {
            return "FAILED";
        }

        return "PENDING";
    }
}
