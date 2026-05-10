package com.aims.payment_service.subsystem.vietqr;

import com.aims.payment_service.entity.Invoice;
import com.aims.payment_service.entity.QRCode;
import com.aims.payment_service.exception.InvalidTokenException;
import com.aims.payment_service.exception.QRCodeGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = VietQRController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VietQRControllerTest {

    @Autowired
    private VietQRController vietQRController;

    @MockitoBean
    private VietQRBoundary boundary;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setPaymentReference("REF-123");
        invoice.setTotalAmount(100000);
    }

    // ==========================================
    // Tests for getValidAccessToken
    // ==========================================

    @Test
    void shouldReturnNewTokenWhenCacheIsEmpty() {
        String mockResponse = "{\"access_token\":\"token123\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(mockResponse);

        String token = vietQRController.getValidAccessToken();

        assertThat(token).isEqualTo("token123");
        verify(boundary, times(1)).getAccessToken(anyString());
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsEmpty() {
        String mockResponse = "{\"access_token\":\"\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(mockResponse);

        assertThrows(InvalidTokenException.class, () -> vietQRController.getValidAccessToken());
    }

    // ==========================================
    // Tests for generateQRCode
    // ==========================================

    @Test
    void shouldGenerateQRCodeSuccessfully() {
        // Mock token
        String tokenResponse = "{\"access_token\":\"token123\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(tokenResponse);

        // Mock QR
        String qrResponse = "{\"code\":\"00\",\"desc\":\"Success\",\"data\":{\"qrCode\":\"000201...\",\"qrDataURL\":\"data:image/png;base64,...\"}}";
        when(boundary.generateQRCode(anyString(), anyString())).thenReturn(qrResponse);

        QRCode qrCode = vietQRController.generateQRCode(invoice);

        assertThat(qrCode).isNotNull();
        assertThat(qrCode.getQrCode()).isEqualTo("000201...");
    }

    @Test
    void shouldThrowQRCodeGenerationExceptionWhenQREmpty() {
        // Mock token
        String tokenResponse = "{\"access_token\":\"token123\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(tokenResponse);

        // Mock empty QR
        String qrResponse = "{\"code\":\"00\",\"desc\":\"Success\",\"data\":{\"qrCode\":\"\",\"qrDataURL\":\"\"}}";
        when(boundary.generateQRCode(anyString(), anyString())).thenReturn(qrResponse);

        assertThrows(QRCodeGenerationException.class, () -> vietQRController.generateQRCode(invoice));
    }

    // ==========================================
    // Tests for checkPaymentStatus
    // ==========================================

    @Test
    void shouldReturnSuccessWhenResponseIs00() {
        // Mock token
        String tokenResponse = "{\"access_token\":\"token123\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(tokenResponse);

        // Mock check
        when(boundary.checkPaymentStatus(anyString(), anyString())).thenReturn("{\"code\":\"00\"}");

        String status = vietQRController.checkPaymentStatus(invoice);

        assertThat(status).isEqualTo("SUCCESS");
    }

    @Test
    void shouldReturnFailedWhenResponseIsFailed() {
        // Mock token
        String tokenResponse = "{\"access_token\":\"token123\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(tokenResponse);

        // Mock check
        when(boundary.checkPaymentStatus(anyString(), anyString())).thenReturn("FAILED transaction");

        String status = vietQRController.checkPaymentStatus(invoice);

        assertThat(status).isEqualTo("FAILED");
    }

    @Test
    void shouldReturnPendingWhenResponseIsUnknown() {
        // Mock token
        String tokenResponse = "{\"access_token\":\"token123\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        when(boundary.getAccessToken(anyString())).thenReturn(tokenResponse);

        // Mock check
        when(boundary.checkPaymentStatus(anyString(), anyString())).thenReturn("{\"code\":\"99\"}"); // Not 00 or failed

        String status = vietQRController.checkPaymentStatus(invoice);

        assertThat(status).isEqualTo("PENDING");
    }
}
