package com.aims.payment_service.subsystem.vietqr.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response khi gọi VietQR token endpoint.
 * VietQR trả về: { "access_token": "...", "token_type": "bearer", "expires_in": 3600 }
 */
@Data
@NoArgsConstructor
public class QRAccessTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenTypes;

    @JsonProperty("expires_in")
    private int expiresIn;

    public void parseResponseString(String response) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            QRAccessTokenResponse parsed = mapper.readValue(response, QRAccessTokenResponse.class);
            this.accessToken = parsed.accessToken;
            this.tokenTypes  = parsed.tokenTypes;
            this.expiresIn   = parsed.expiresIn;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token response: " + e.getMessage(), e);
        }
    }
}
