package com.aims.aimsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO đại diện cho body mà VietQR POST về endpoint webhook của chúng ta.
 * VietQR gọi: POST <ngrok-url>/api/payment/webhook
 *
 * Các field theo spec của VietQR sandbox callback.
 */
@Data
public class WebhookRequest {

    /**
     * Mã tham chiếu giao dịch — khớp với externalTransactionId chúng ta đã gửi đi
     * khi kích hoạt giả lập (transactionRefId trong spec VietQR).
     */
    @JsonProperty("transactionRefId")
    private String transactionRefId;

    /** Số tiền giao dịch */
    @JsonProperty("amount")
    private BigDecimal amount;

    /** Mã giao dịch nội bộ VietQR */
    @JsonProperty("transactionId")
    private String transactionId;

    /** Thời điểm giao dịch (do VietQR trả về) */
    @JsonProperty("transactionDate")
    private String transactionDate;

    /** Nội dung chuyển khoản */
    @JsonProperty("content")
    private String content;

    /**
     * Trích xuất mã giao dịch (externalTransactionId) từ trường content.
     * VietQR có thể không gửi transactionRefId trong payload nhưng sẽ có trong content.
     * Ví dụ content: "AIMS REF-A1B2C3D4" -> trả về "REF-A1B2C3D4"
     */
    public String extractTransactionRefId() {
        if (transactionRefId != null && !transactionRefId.isBlank()) {
            return transactionRefId;
        }
        if (content == null || content.isBlank()) {
            return null;
        }
        // Tìm pattern REF- kèm chuỗi ký tự/số
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(REF-[A-Z0-9]+)").matcher(content);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
