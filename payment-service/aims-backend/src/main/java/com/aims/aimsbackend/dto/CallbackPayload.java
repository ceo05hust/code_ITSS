package com.aims.aimsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Payload mà VietQR gửi đến callback endpoint của chúng ta.
 * POST /vqr/bank/api/test/transaction-callback
 */
@Data
public class CallbackPayload {

    @JsonProperty("transactionid")
    private String transactionId;

    @JsonProperty("transactionRefId")
    private String transactionRefId;     // = invoiceId chúng ta gửi lúc generate QR

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("content")
    private String content;

    @JsonProperty("bankAccount")
    private String bankAccount;

    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("userBankName")
    private String userBankName;

    @JsonProperty("refNumber")
    private String refNumber;

    @JsonProperty("terminalCode")
    private String terminalCode;

    @JsonProperty("note")
    private String note;

    @JsonProperty("time")
    private long time;

    /** "00" = thành công */
    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    /**
     * VietQR Sandbox không gửi trường "status" trong callback thực tế.
     * Việc VietQR gọi về endpoint của chúng ta = giao dịch đã xảy ra thành công.
     * Chỉ reject khi status rõ ràng là FAILED.
     */
    public boolean isSuccess() {
        if (status == null || status.isBlank()) return true; // null = VietQR callback = thành công
        return "00".equals(status) || "SUCCESS".equalsIgnoreCase(status);
    }

    /**
     * Lấy paymentRef từ content nếu transactionRefId null.
     * VietQR gửi content = "AIMS REF-A1B2C3D4" → trích xuất "REF-A1B2C3D4".
     */
    public String extractInvoiceIdFromContent() {
        if (transactionRefId != null && !transactionRefId.isBlank()) return transactionRefId;
        if (content == null) return null;
        // Tìm pattern "AIMS REF-XXXXXXXX" (paymentRef mới)
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("AIMS\\s+(REF-[A-Z0-9]+)").matcher(content);
        if (m.find()) return m.group(1);
        // Fallback: "AIMS <số>" (invoiceId cũ)
        java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("AIMS\\s+(\\d+)").matcher(content);
        return m2.find() ? m2.group(1) : null;
    }
}
