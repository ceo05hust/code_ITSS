package com.aims.aimsbackend.subsystem.vietqr.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QRAccessTokenRequest extends QRRequest {

    private String userName;
    private String password;

    public String buildAuthorizationHeader() {
        String credentials = userName + ":" + password;
        String encoded     = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }

    @Override
    public String buildRequestString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(new Body(userName, password));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build token request: " + e.getMessage(), e);
        }
    }

    private record Body(String userName, String password) {}
}
