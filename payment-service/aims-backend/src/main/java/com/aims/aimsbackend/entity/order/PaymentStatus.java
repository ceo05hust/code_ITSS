package com.aims.aimsbackend.entity.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentStatus {

    /** "00" = thành công, các giá trị khác = thất bại */
    private String status;
    private String message;

    public PaymentStatus(String status, String message) {
        this.status  = status;
        this.message = message;
    }

    /**
     * Parse JSON response string từ VietQR callback / status API.
     */
    public void parseResponseString(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root       = mapper.readTree(response);
            JsonNode data       = root.has("data") ? root.get("data") : root;

            this.status  = getTextSafe(data, "status");
            this.message = getTextSafe(data, "message");

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PaymentStatus response: " + e.getMessage(), e);
        }
    }

    /** Kiểm tra payment có thành công không (VietQR dùng "00" là success) */
    public boolean isSuccess() {
        return "00".equals(status) || "SUCCESS".equalsIgnoreCase(status);
    }

    private String getTextSafe(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText("") : "";
    }
}
