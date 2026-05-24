package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.exception.order.UnknownException;
import com.aims.aimsbackend.subsystem.vietqr.response.QRAccessTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class VietQRBoundary {

    @Value("${vietqr.api.token-url}")
    private String GET_TOKEN_URL;

    @Value("${vietqr.api.generate-qr-url}")
    private String GENERATE_QR_URL;

    @Value("${vietqr.api.test-callback-url}")
    private String TEST_CALLBACK_URL;

    private final RestTemplate restTemplate;

    public VietQRBoundary(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    String getAccessToken(String authorizationHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    GET_TOKEN_URL, HttpMethod.POST, request, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new InvalidTokenException("VietQR token endpoint returned: " + response.getStatusCode());
            }

            log.info("VietQR getAccessToken success");
            return response.getBody();

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("VietQR getAccessToken failed: {}", e.getMessage());
            throw new InvalidTokenException("Cannot get VietQR access token: " + e.getMessage());
        }
    }

    String generateQRCode(String requestString, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken);

            HttpEntity<String> request = new HttpEntity<>(requestString, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    GENERATE_QR_URL, HttpMethod.POST, request, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new QRCodeGenerationException("VietQR generate QR failed: " + response.getStatusCode());
            }

            log.info("VietQR generateQRCode success");
            return response.getBody();

        } catch (QRCodeGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("VietQR generateQRCode failed: {}", e.getMessage());
            throw new QRCodeGenerationException("Cannot generate QR: " + e.getMessage());
        }
    }

    String checkPaymentStatus(String requestString, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken);

            HttpEntity<String> request = new HttpEntity<>(requestString, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    TEST_CALLBACK_URL, HttpMethod.POST, request, String.class
            );

            log.info("VietQR checkPaymentStatus response: {}", response.getStatusCode());
            return response.getBody() != null ? response.getBody() : "{}";

        } catch (Exception e) {
            log.error("VietQR checkPaymentStatus failed: {}", e.getMessage());
            throw new UnknownException("Cannot check payment status: " + e.getMessage());
        }
    }
}
