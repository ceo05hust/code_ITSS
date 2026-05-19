package com.aims.aimsbackend.controller;

import com.aims.aimsbackend.dto.InvoiceRequest;
import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.QRCode;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.exception.order.CallbackValidationException;
import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.PaymentTimeoutException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.service.order.PaymentCacheService;
import com.aims.aimsbackend.subsystem.vietqr.IPaymentQRCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayOrderController.class)
class PayOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IPaymentQRCode paymentQRCode;

    @MockitoBean
    private PaymentCacheService paymentCacheService;

    private Map<String, Object> validInvoiceRequest;

    @BeforeEach
    void setUp() {
        validInvoiceRequest = Map.of(
                "invoiceId", 1,
                "shippingFee", 10000,
                "totalProductPriceExVAT", 90000,
                "totalProductPriceIncVAT", 100000,
                "totalAmount", 110000
        );
    }

    // ==========================================
    // Tests for /api/payment/generate-qr
    // ==========================================

    @Test
    void shouldGenerateQRCodeSuccessfully() throws Exception {
        QRCode mockQr = new QRCode();
        mockQr.setQrCode("000201010211...");
        mockQr.setBankCode("BIDV");

        when(paymentQRCode.generateQRCode(any(Invoice.class))).thenReturn(mockQr);

        mockMvc.perform(post("/api/payment/generate-qr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validInvoiceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("QR code generated"))
                .andExpect(jsonPath("$.data.qrCode.qrCode").value("000201010211..."));
    }

    @Test
    void shouldHandleInvalidTokenExceptionWhenGeneratingQR() throws Exception {
        when(paymentQRCode.generateQRCode(any(Invoice.class)))
                .thenThrow(new InvalidTokenException("Token expired"));

        mockMvc.perform(post("/api/payment/generate-qr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validInvoiceRequest)))
                .andExpect(status().isServiceUnavailable()) // 503
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Service unavailable: Token expired"));
    }

    @Test
    void shouldHandleQRCodeGenerationException() throws Exception {
        when(paymentQRCode.generateQRCode(any(Invoice.class)))
                .thenThrow(new QRCodeGenerationException("Bank API down"));

        mockMvc.perform(post("/api/payment/generate-qr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validInvoiceRequest)))
                .andExpect(status().isBadGateway()) // 502
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldHandleUnknownExceptionWhenGeneratingQR() throws Exception {
        when(paymentQRCode.generateQRCode(any(Invoice.class)))
                .thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(post("/api/payment/generate-qr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validInvoiceRequest)))
                .andExpect(status().isInternalServerError()) // Runtime is caught as UnknownException inside or by global handler (500)
                // In controller it throws UnknownException which might map to 500 depending on ControllerAdvice. Assuming standard behaviour.
                .andExpect(jsonPath("$.success").value(false));
    }


    // ==========================================
    // Tests for /api/payment/confirm
    // ==========================================

    @Test
    void shouldConfirmPaymentImmediatelyIfCacheHit() throws Exception {
        String ref = "REF-TEST";
        TransactionInfo mockTxn = new TransactionInfo();
        mockTxn.setTransactionId(123);

        when(paymentCacheService.getCompletedPayment(ref)).thenReturn(mockTxn);

        mockMvc.perform(post("/api/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentRef\": \"REF-TEST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void shouldReturnPendingIfWebhookSlow() throws Exception {
        String ref = "REF-TEST";
        when(paymentCacheService.getCompletedPayment(ref)).thenReturn(null);
        when(paymentCacheService.getPendingInvoice(ref)).thenReturn(new Invoice());
        when(paymentQRCode.checkPaymentStatus(any(Invoice.class))).thenReturn("PENDING");

        mockMvc.perform(post("/api/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentRef\": \"REF-TEST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldThrowUnknownExceptionIfSessionNotFound() throws Exception {
        when(paymentCacheService.getCompletedPayment("REF-TEST")).thenReturn(null);
        when(paymentCacheService.getPendingInvoice("REF-TEST")).thenReturn(null);

        mockMvc.perform(post("/api/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentRef\": \"REF-TEST\"}"))
                .andExpect(status().isInternalServerError()); // Mapping for UnknownException without @ResponseStatus
    }

    @Test
    void shouldHandlePaymentTimeoutException() throws Exception {
        when(paymentCacheService.getCompletedPayment(anyString()))
                .thenThrow(new PaymentTimeoutException("Timeout"));

        mockMvc.perform(post("/api/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentRef\": \"REF-TEST\"}"))
                .andExpect(status().isRequestTimeout()); // 408
    }

    @Test
    void shouldHandleCallbackValidationException() throws Exception {
        when(paymentCacheService.getCompletedPayment(anyString()))
                .thenThrow(new CallbackValidationException("Tampered"));

        mockMvc.perform(post("/api/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentRef\": \"REF-TEST\"}"))
                .andExpect(status().isBadRequest()); // 400
    }

    // ==========================================
    // Tests for /api/payment/switch-method
    // ==========================================

    @Test
    void shouldSwitchMethodToPayPal() throws Exception {
        mockMvc.perform(post("/api/payment/switch-method")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentRef\": \"REF-TEST\", \"method\": \"PayPal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.method").value("PayPal"));
    }

    // ==========================================
    // Tests for /api/payment/transaction/{ref}
    // ==========================================

    @Test
    void shouldReturnTransactionIfFound() throws Exception {
        TransactionInfo txn = new TransactionInfo();
        txn.setAmount(500);
        when(paymentCacheService.getCompletedPayment("REF-TEST")).thenReturn(txn);

        mockMvc.perform(get("/api/payment/transaction/REF-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(500.0));
    }
}
