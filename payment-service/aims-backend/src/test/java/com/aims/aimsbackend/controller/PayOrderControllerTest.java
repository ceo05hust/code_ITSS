package com.aims.aimsbackend.controller;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.dto.SimulatePaymentRequest;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.service.order.PaymentService;
import com.aims.aimsbackend.service.order.TransactionInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayOrderController.class)
public class PayOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TransactionInfoService transactionInfoService;

    @Autowired
    private ObjectMapper objectMapper;

    private String testExternalId;

    @BeforeEach
    void setUp() {
        testExternalId = "REF-TEST1234";
    }

    @Test
    void testGenerateQRCode_Success() throws Exception {
        InvoiceResponse invoice = new InvoiceResponse();
        invoice.setTotalAmount(new BigDecimal("500000"));

        QRCode mockQr = new QRCode();
        mockQr.setQrCode("12345");
        PaymentService.GenerateQRResult mockResult = new PaymentService.GenerateQRResult(testExternalId, mockQr);

        when(paymentService.generateVietQR(any(InvoiceResponse.class))).thenReturn(mockResult);

        mockMvc.perform(post("/api/payment/generate-qr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.externalTransactionId").value(testExternalId))
                .andExpect(jsonPath("$.data.qrCode.qrCode").value("12345"));
    }

    @Test
    void testGenerateQRCode_Failure() throws Exception {
        InvoiceResponse invoice = new InvoiceResponse();

        when(paymentService.generateVietQR(any(InvoiceResponse.class)))
                .thenThrow(new QRCodeGenerationException("API error"));

        mockMvc.perform(post("/api/payment/generate-qr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoice)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("QR generation failed: API error"));
    }

    @Test
    void testSimulatePayment_Success() throws Exception {
        SimulatePaymentRequest request = new SimulatePaymentRequest();
        request.setExternalTransactionId(testExternalId);
        request.setAmount(new BigDecimal("500000"));

        doNothing().when(paymentService).simulatePayment(any(SimulatePaymentRequest.class));

        mockMvc.perform(post("/api/payment/simulate-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetTransactionStatus_Success() throws Exception {
        TransactionInfo txn = new TransactionInfo();
        txn.setTransactionId(1L);
        txn.setExternalTransactionId(testExternalId);
        txn.setAmount(new BigDecimal("500000"));

        when(transactionInfoService.findByExternalTransactionId(testExternalId)).thenReturn(txn);

        mockMvc.perform(get("/api/payment/transaction/{id}", testExternalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transactionId").value(1))
                .andExpect(jsonPath("$.data.externalTransactionId").value(testExternalId));
    }

    @Test
    void testGetTransactionStatus_Pending() throws Exception {
        when(transactionInfoService.findByExternalTransactionId(testExternalId)).thenReturn(null);

        mockMvc.perform(get("/api/payment/transaction/{id}", testExternalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.externalTransactionId").value(testExternalId));
    }

    @Test
    void testSwitchMethod() throws Exception {
        Map<String, String> payload = Map.of(
                "externalTransactionId", testExternalId,
                "method", "PayPal"
        );

        mockMvc.perform(post("/api/payment/switch-method")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Switched to PayPal"));
    }
}
