package com.aims.aimsbackend.subsystem.vietqr.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QRCode {

    private String qrCode;
    private String qrLink;
    private String bankCode;
    private String bankName;
    private String bankAccount;

    /**
     * Parse JSON response string từ VietQR generate QR API.
     * VietQR trả về dạng: { "data": { "qrCode": "...", "qrLink": "...", ... } }
     */
    public void parseQRCodeResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // VietQR API wrapper: root -> data
            JsonNode data = root.has("data") ? root.get("data") : root;

            this.qrCode    = getTextSafe(data, "qrCode");
            this.qrLink    = getTextSafe(data, "qrLink");
            this.bankCode  = getTextSafe(data, "bankCode");
            this.bankName  = getTextSafe(data, "bankName");
            this.bankAccount = getTextSafe(data, "bankAccount");

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse QR code response: " + e.getMessage(), e);
        }
    }

    private String getTextSafe(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText("") : "";
    }
}
