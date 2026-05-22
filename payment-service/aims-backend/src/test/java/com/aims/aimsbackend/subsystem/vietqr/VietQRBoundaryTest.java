package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VietQRBoundaryTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private VietQRBoundary vietQRBoundary;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vietQRBoundary, "GET_TOKEN_URL", "http://token-url");
        ReflectionTestUtils.setField(vietQRBoundary, "GENERATE_QR_URL", "http://generate-url");
        ReflectionTestUtils.setField(vietQRBoundary, "TEST_CALLBACK_URL", "http://callback-url");
    }

    @Test
    void testGetAccessToken_Success() {
        String mockResponse = "{\"code\":\"00\", \"data\":{\"accessToken\":\"test-token\"}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://token-url"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        String result = vietQRBoundary.getAccessToken("Basic auth");

        assertEquals(mockResponse, result);
    }

    @Test
    void testGetAccessToken_FailedApi() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        assertThrows(InvalidTokenException.class, () -> {
            vietQRBoundary.getAccessToken("Basic auth");
        });
    }

    @Test
    void testGetAccessToken_Exception() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThrows(InvalidTokenException.class, () -> {
            vietQRBoundary.getAccessToken("Basic auth");
        });
    }

    @Test
    void testGenerateQRCode_Success() {
        String mockResponse = "{\"code\":\"00\", \"data\":{\"qrCode\":\"123\"}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://generate-url"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        String result = vietQRBoundary.generateQRCode("request-body", "bearer-token");

        assertEquals(mockResponse, result);
    }

    @Test
    void testGenerateQRCode_FailedApi() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        assertThrows(QRCodeGenerationException.class, () -> {
            vietQRBoundary.generateQRCode("request-body", "bearer-token");
        });
    }

    @Test
    void testCheckPaymentStatus_Success() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Success", HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://callback-url"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        assertDoesNotThrow(() -> {
            vietQRBoundary.checkPaymentStatus("request-body", "bearer-token");
        });
    }

    @Test
    void testCheckPaymentStatus_Failed() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThrows(RuntimeException.class, () -> {
            vietQRBoundary.checkPaymentStatus("request-body", "bearer-token");
        });
    }
}
