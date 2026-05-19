package com.aims.aimsbackend.subsystem.vietqr.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

/**
 * Request để generate QR Code từ VietQR API.
 * Các field theo class diagram + VietQR API specification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRGenerateRequest extends QRRequest {

    private String bankCode;
    private String bankName;
    private String bankAccount;
    private String userBankName;
    private double amount;
    private String content;

    private Integer qrType;
    private String orderId;
    private String transType;

    private String qrCode;
    private String qrLink;
    private String terminalCode;
    private String subTerminalCode;
    private String note;
    private String urlLink;
    private String additionalData;
    private String vaAccount;

    @Override
    public String buildRequestString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build QR generate request: " + e.getMessage(), e);
        }
    }
}
