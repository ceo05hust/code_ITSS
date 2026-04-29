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

    public boolean isSuccess() {
        return "00".equals(status) || "SUCCESS".equalsIgnoreCase(status);
    }
}
