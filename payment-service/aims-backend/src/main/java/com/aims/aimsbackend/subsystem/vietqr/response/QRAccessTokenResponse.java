package com.aims.aimsbackend.subsystem.vietqr.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

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
