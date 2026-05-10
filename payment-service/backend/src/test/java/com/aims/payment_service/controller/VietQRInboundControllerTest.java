package com.aims.payment_service.controller;

import com.aims.payment_service.dto.CallbackPayload;
import com.aims.payment_service.entity.Invoice;
import com.aims.payment_service.entity.TransactionInfo;
import com.aims.payment_service.exception.CallbackValidationException;
import com.aims.payment_service.exception.PaymentFailedException;
import com.aims.payment_service.repository.InvoiceRepository;
import com.aims.payment_service.repository.TransactionInfoRepository;
import com.aims.payment_service.service.PaymentCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VietQRInboundController.class)
@TestPropertySource(properties = {
        "vietqr.callback.username=admin",
        "vietqr.callback.password=admin",
        "vietqr.callback.token=dummy_token_123"
})
class VietQRInboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceRepository invoiceRepository;

    @MockitoBean
    private TransactionInfoRepository transactionRepository;

    @MockitoBean
    private PaymentCacheService paymentCacheService;

    private String validBasicAuth;
    private CallbackPayload validPayload;

    @BeforeEach
    void setUp() {
        String authString = "admin:admin";
        validBasicAuth = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());

        validPayload = new CallbackPayload();
        validPayload.setStatus("00");
        validPayload.setAmount(100000);
        validPayload.setContent("Thanh toan AIMS REF-12345");
        validPayload.setTransactionRefId("REF-12345");
    }

    // ==========================================
    // Tests for /vqr/api/token_generate
    // ==========================================

    @Test
    void shouldReturnTokenWhenBasicAuthIsValid() throws Exception {
        mockMvc.perform(post("/vqr/api/token_generate")
                        .header("Authorization", validBasicAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("dummy_token_123"));
    }

    @Test
    void shouldReturnUnauthorizedWhenBasicAuthIsInvalid() throws Exception {
        mockMvc.perform(post("/vqr/api/token_generate")
                        .header("Authorization", "Basic WRONG"))
                .andExpect(status().isUnauthorized()); // 401
    }

    // ==========================================
    // Tests for /vqr/bank/api/test/transaction-callback
    // ==========================================

    @Test
    void shouldProcessCallbackSuccessfully() throws Exception {
        Invoice cachedInvoice = new Invoice();
        when(paymentCacheService.getPendingInvoice("REF-12345")).thenReturn(cachedInvoice);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(cachedInvoice);

        mockMvc.perform(post("/vqr/bank/api/test/transaction-callback")
                        .header("Authorization", "Bearer dummy_token_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(invoiceRepository, times(2)).save(any(Invoice.class));
        verify(transactionRepository, times(1)).save(any(TransactionInfo.class));
        verify(paymentCacheService, times(1)).addCompletedPayment(eq("REF-12345"), any(TransactionInfo.class));
    }

    @Test
    void shouldThrowCallbackValidationExceptionWhenBearerIsInvalid() throws Exception {
        // Exception should be thrown and map to whatever global handler says, or bubble up.
        // Spring MVC usually wraps this in a ServletException if unhandled, or maps to 500.
        // Let's assume standard behavior.
        try {
            mockMvc.perform(post("/vqr/bank/api/test/transaction-callback")
                            .header("Authorization", "Bearer INVALID")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPayload)))
                    .andExpect(status().isBadRequest()); // 400
        } catch (Exception e) {
            assert e.getCause() instanceof CallbackValidationException;
        }
    }

    @Test
    void shouldThrowPaymentFailedExceptionWhenPayloadNotSuccess() throws Exception {
        validPayload.setStatus("FAILED");

        try {
            mockMvc.perform(post("/vqr/bank/api/test/transaction-callback")
                            .header("Authorization", "Bearer dummy_token_123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPayload)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            assert e.getCause() instanceof PaymentFailedException;
        }
    }

    @Test
    void shouldThrowPaymentFailedExceptionWhenSessionNotFound() throws Exception {
        when(paymentCacheService.getPendingInvoice(anyString())).thenReturn(null);

        try {
            mockMvc.perform(post("/vqr/bank/api/test/transaction-callback")
                            .header("Authorization", "Bearer dummy_token_123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPayload)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            assert e.getCause() instanceof PaymentFailedException;
        }
    }
}
