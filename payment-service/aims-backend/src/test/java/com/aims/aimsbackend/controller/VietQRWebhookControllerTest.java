package com.aims.aimsbackend.controller;

import com.aims.aimsbackend.dto.WebhookRequest;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.service.order.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VietQRWebhookController.class)
public class VietQRWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testReceiveWebhook_Success() throws Exception {
        WebhookRequest webhookRequest = new WebhookRequest();
        webhookRequest.setTransactionRefId("REF-TEST1234");
        webhookRequest.setAmount(new BigDecimal("500000"));

        TransactionInfo mockTxn = new TransactionInfo();
        mockTxn.setTransactionId(1L);
        mockTxn.setExternalTransactionId("REF-TEST1234");

        when(paymentService.processWebhook(any(WebhookRequest.class))).thenReturn(mockTxn);

        mockMvc.perform(post("/vqr/bank/api/test/transaction-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Callback processed successfully"));

        verify(paymentService, times(1)).processWebhook(any(WebhookRequest.class));
    }
}
