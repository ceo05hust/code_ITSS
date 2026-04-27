package com.aims.payment_service.subsystem.vietqr.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Base64;

/**
 * Request để lấy Access Token từ VietQR API.
 * VietQR xác thực bằng Basic Auth: Base64(username:password)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QRAccessTokenRequest extends QRRequest {

    private String userName;
    private String password;

    /**
     * Tạo Authorization header dạng: "Basic Base64(username:password)"
     */
    public String buildAuthorizationHeader() {
        String credentials = userName + ":" + password;
        String encoded     = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }

    /**
     * VietQR token endpoint nhận body JSON với userName và password.
     */
    @Override
    public String buildRequestString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(new Body(userName, password));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build token request: " + e.getMessage(), e);
        }
    }

    /** Inner record dùng để serialize JSON body */
    private record Body(String userName, String password) {}
}
