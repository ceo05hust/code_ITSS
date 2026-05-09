package com.aims.payment_service.dto;

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
     * Lấy invoiceId từ content nếu transactionRefId null.
     * VietQR Sandbox gửi content = "VQRxxxxx AIMS 10" → trích xuất số 10.
     */
    public String extractInvoiceIdFromContent() {
        if (transactionRefId != null && !transactionRefId.isBlank()) return transactionRefId;
        if (content == null) return null;
        // Tìm pattern "AIMS <số>" trong content
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("AIMS\\s+(\\d+)").matcher(content);
        return m.find() ? m.group(1) : null;
    }
}
