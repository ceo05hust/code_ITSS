package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;
import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
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

    private InvoiceResponse invoiceResponse;
    private static final String EXTERNAL_TRANSACTION_ID = "REF-TESTREF1";

    @BeforeEach
    void setUp() {
        invoiceResponse = new InvoiceResponse();
        invoiceResponse.setTotalAmount(java.math.BigDecimal.valueOf(507000));
        invoiceResponse.setShippingFee(java.math.BigDecimal.valueOf(12000));
        invoiceResponse.setTotalProductPriceExclVAT(java.math.BigDecimal.valueOf(450000));
        invoiceResponse.setTotalProductPriceInclVAT(java.math.BigDecimal.valueOf(495000));
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

        // Mock QR response
        String qrResponse = "{\"code\":\"00\",\"desc\":\"Success\",\"data\":{\"qrCode\":\"000201...\",\"qrDataURL\":\"data:image/png;base64,...\"}}";
        when(boundary.generateQRCode(anyString(), anyString())).thenReturn(qrResponse);

        QRCode qrCode = vietQRController.generateQRCode(invoiceResponse, EXTERNAL_TRANSACTION_ID);

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

        assertThrows(QRCodeGenerationException.class,
                () -> vietQRController.generateQRCode(invoiceResponse, EXTERNAL_TRANSACTION_ID));
    }
}
