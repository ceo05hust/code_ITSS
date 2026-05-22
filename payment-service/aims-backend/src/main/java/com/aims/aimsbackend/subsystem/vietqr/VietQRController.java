package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;
import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.exception.order.UnknownException;
import com.aims.aimsbackend.subsystem.vietqr.request.QRAccessTokenRequest;
import com.aims.aimsbackend.subsystem.vietqr.request.QRGenerateRequest;
import com.aims.aimsbackend.subsystem.vietqr.request.TestCallbackRequest;
import com.aims.aimsbackend.subsystem.vietqr.response.QRAccessTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * VietQRController — Tầng giao tiếp với VietQR external API.
 *
 * Implements IPaymentQRCode và đóng gói toàn bộ logic gọi API VietQR:
 *   1. Lấy Access Token (có cache, tự refresh khi hết hạn)
 *   2. Tạo mã QR thanh toán
 *   3. Kích hoạt giả lập thanh toán thành công qua test-callback
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

    // ---- Token cache ----
    private String  cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public VietQRController(VietQRBoundary boundary) {
        this.boundary = boundary;
    }

    // =========================================================
    // 1. Get Access Token (with cache)
    // =========================================================

    /**
     * Lấy access token hợp lệ từ VietQR.
     * Token được cache lại và tự động làm mới khi hết hạn.
     */
    public String getValidAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            log.debug("Using cached VietQR access token");
            return cachedToken;
        }

        log.info("Fetching new VietQR access token...");
        QRAccessTokenRequest tokenRequest = new QRAccessTokenRequest(apiUsername, apiPassword);
        String authHeader  = tokenRequest.buildAuthorizationHeader();
        String rawResponse = boundary.getAccessToken(authHeader);

        QRAccessTokenResponse tokenResponse = new QRAccessTokenResponse();
        tokenResponse.parseResponseString(rawResponse);

        if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new InvalidTokenException("VietQR returned empty access token");
        }

        this.cachedToken = tokenResponse.getAccessToken();
        // Giảm 60 giây để an toàn trước khi hết hạn
        this.tokenExpiry = Instant.now().plusSeconds(
                Math.max(tokenResponse.getExpiresIn() - 60, 60)
        );

        log.info("VietQR access token acquired, expires in {}s", tokenResponse.getExpiresIn());
        return cachedToken;
    }

    // =========================================================
    // 2. Generate QR Code
    // =========================================================

    /**
     * Tạo mã QR thanh toán từ thông tin đơn hàng.
     *
     * @param response              Thông tin đơn hàng phản hồi từ hệ thống (số tiền, phí ship, v.v.)
     * @param externalTransactionId Mã tham chiếu giao dịch duy nhất
     * @return QRCode               Đối tượng chứa URL ảnh QR và data QR
     */
    @Override
    public QRCode generateQRCode(InvoiceResponse response, String externalTransactionId) {
        String token = getValidAccessToken();

        QRGenerateRequest generateRequest = QRGenerateRequest.builder()
                .bankCode(bankCode)
                .bankName(bankName)
                .bankAccount(bankAccount)
                .userBankName(bankUserName)
                .amount(response.getTotalAmount().doubleValue())
                .content("AIMS " + externalTransactionId)   // Nội dung chuyển khoản
                .qrType(0)
                .orderId(externalTransactionId)
                .transType("C")
                .build();

        String rawResponse = boundary.generateQRCode(generateRequest.buildRequestString(), token);

        QRCode qrCode = new QRCode();
        qrCode.parseQRCodeResponse(rawResponse);

        if (qrCode.getQrCode() == null || qrCode.getQrCode().isBlank()) {
            throw new QRCodeGenerationException("VietQR returned empty QR code for externalTransactionId: " + externalTransactionId);
        }

        return qrCode;
    }

    // =========================================================
    // 3. Trigger Test Callback (Giả lập thanh toán)
    // =========================================================

    /**
     * Kích hoạt giả lập thanh toán thành công bằng cách gọi VietQR test-callback API.
     * VietQR sẽ nhận request này rồi gọi lại webhook của chúng ta (callbackUrl).
     *
     * @param externalTransactionId Mã tham chiếu giao dịch — khớp với mã đã dùng khi tạo QR
     * @param amount                Số tiền cần giả lập
     */
    @Override
    public void triggerTestCallback(String externalTransactionId, BigDecimal amount) {
        log.info("Triggering test callback — externalTransactionId={}, amount={}", externalTransactionId, amount);

        try {
            String token = getValidAccessToken();

            TestCallbackRequest callbackRequest = TestCallbackRequest.builder()
                    .bankAccount(bankAccount)
                    .bankCode(bankCode)
                    .amount(amount.longValue())
                    .content("AIMS " + externalTransactionId)
                    .transType("C")
                    .build();

            String rawResponse = boundary.checkPaymentStatus(callbackRequest.buildRequestString(), token);
            log.info("Test callback triggered successfully — response: {}", rawResponse);

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("VietQR Sandbox API returned an error, but the webhook might still be processing. Error: {}", e.getMessage());
            // Do not throw exception because VietQR Sandbox often returns 400 Bad Request
            // even when it successfully dispatches the webhook.
        }
    }
}
