package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.dto.SimulatePaymentRequest;
import com.aims.aimsbackend.dto.WebhookRequest;
import com.aims.aimsbackend.entity.enums.PaymentMethod;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.exception.order.CallbackValidationException;
import com.aims.aimsbackend.exception.order.PaymentFailedException;
import com.aims.aimsbackend.subsystem.vietqr.IPaymentQRCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private IPaymentQRCode vietQRService;

    @Mock
    private TransactionInfoService transactionInfoService;

    @InjectMocks
    private PaymentService paymentService;

    private String testExternalId;

    @BeforeEach
    void setUp() {
        testExternalId = "REF-TEST1234";
    }

    @Test
    void testGenerateVietQR() {
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        QRCode mockQr = new QRCode();
        mockQr.setQrCode("000201010212...");

        // vietQRService.generateQRCode returns mockQr
        when(vietQRService.generateQRCode(eq(invoiceResponse), anyString())).thenReturn(mockQr);

        PaymentService.GenerateQRResult result = paymentService.generateVietQR(invoiceResponse);

        assertNotNull(result);
        assertNotNull(result.externalTransactionId());
        assertTrue(result.externalTransactionId().startsWith("REF-"));
        assertEquals(mockQr, result.qrCode());

        verify(vietQRService, times(1)).generateQRCode(eq(invoiceResponse), anyString());
    }

    @Test
    void testSimulatePayment() {
        SimulatePaymentRequest request = new SimulatePaymentRequest();
        request.setExternalTransactionId(testExternalId);
        request.setAmount(new BigDecimal("500000"));

        paymentService.simulatePayment(request);

        verify(vietQRService, times(1)).triggerTestCallback(testExternalId, new BigDecimal("500000"));
    }

    @Test
    void testProcessWebhook_Success() {
        WebhookRequest webhookRequest = new WebhookRequest();
        webhookRequest.setTransactionRefId(testExternalId);
        webhookRequest.setAmount(new BigDecimal("500000"));
        webhookRequest.setContent("Test payment");

        // Giả lập chưa thanh toán
        when(transactionInfoService.findByExternalTransactionId(testExternalId)).thenReturn(null);

        TransactionInfo mockTransactionInfo = new TransactionInfo();
        mockTransactionInfo.setTransactionId(1L);
        mockTransactionInfo.setExternalTransactionId(testExternalId);

        when(transactionInfoService.createTransactionInfo(
                eq(testExternalId), eq("Test payment"), eq(new BigDecimal("500000")), eq(PaymentMethod.VIETQR)
        )).thenReturn(mockTransactionInfo);

        TransactionInfo result = paymentService.processWebhook(webhookRequest);

        assertNotNull(result);
        assertEquals(1, result.getTransactionId());
        assertEquals(testExternalId, result.getExternalTransactionId());

        verify(transactionInfoService, times(1)).createTransactionInfo(anyString(), anyString(), any(BigDecimal.class), any());
    }

    @Test
    void testProcessWebhook_DuplicateTransaction() {
        WebhookRequest webhookRequest = new WebhookRequest();
        webhookRequest.setTransactionRefId(testExternalId);

        // Giả lập giao dịch đã thanh toán
        TransactionInfo existingTxn = new TransactionInfo();
        existingTxn.setExternalTransactionId(testExternalId);
        when(transactionInfoService.findByExternalTransactionId(testExternalId)).thenReturn(existingTxn);

        Exception exception = assertThrows(PaymentFailedException.class, () -> {
            paymentService.processWebhook(webhookRequest);
        });

        assertEquals("Transaction has already been paid.", exception.getMessage());
        verify(transactionInfoService, never()).createTransactionInfo(anyString(), anyString(), any(), any());
    }
}
