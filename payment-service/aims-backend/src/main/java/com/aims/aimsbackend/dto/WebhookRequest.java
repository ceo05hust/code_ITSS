package com.aims.aimsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WebhookRequest {

    @JsonProperty("transactionRefId")
    private String transactionRefId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("content")
    private String content;

    public String extractTransactionRefId() {
        if (transactionRefId != null && !transactionRefId.isBlank()) {
            return transactionRefId;
        }
        if (content == null || content.isBlank()) {
            return null;
        }
        
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(REF-[A-Z0-9]+)").matcher(content);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
