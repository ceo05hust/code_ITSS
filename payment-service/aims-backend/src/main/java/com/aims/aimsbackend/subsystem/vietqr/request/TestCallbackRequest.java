package com.aims.aimsbackend.subsystem.vietqr.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestCallbackRequest extends QRRequest {

    private String bankAccount;

    private String content;

    private long amount;

    private String bankCode;

    private String transType;

    @Override
    public String buildRequestString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build TestCallback request: " + e.getMessage(), e);
        }
    }
}
