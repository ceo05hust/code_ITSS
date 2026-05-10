package com.aims.payment_service.service;

import com.aims.payment_service.entity.Invoice;
import com.aims.payment_service.entity.TransactionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCacheServiceTest {

    private PaymentCacheService paymentCacheService;

    @BeforeEach
    void setUp() {
        paymentCacheService = new PaymentCacheService();
    }

    @Test
    void shouldAddAndGetPendingInvoice() {
        // Given
        String paymentRef = "REF-TEST-123";
        Invoice invoice = Invoice.builder()
                .totalAmount(100000)
                .build();

        // When
        paymentCacheService.addPendingInvoice(paymentRef, invoice);
        Invoice retrieved = paymentCacheService.getPendingInvoice(paymentRef);

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTotalAmount()).isEqualTo(100000);
    }

    @Test
    void shouldRemovePendingInvoice() {
        // Given
        String paymentRef = "REF-REMOVE";
        Invoice invoice = Invoice.builder().build();
        paymentCacheService.addPendingInvoice(paymentRef, invoice);

        // When
        paymentCacheService.removePendingInvoice(paymentRef);
        Invoice retrieved = paymentCacheService.getPendingInvoice(paymentRef);

        // Then
        assertThat(retrieved).isNull();
    }

    @Test
    void shouldAddAndGetCompletedPayment() {
        // Given
        String paymentRef = "REF-COMPLETE";
        TransactionInfo transactionInfo = TransactionInfo.builder()
                .status("SUCCESS")
                .amount(50000)
                .build();

        // When
        paymentCacheService.addCompletedPayment(paymentRef, transactionInfo);
        TransactionInfo retrieved = paymentCacheService.getCompletedPayment(paymentRef);

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getStatus()).isEqualTo("SUCCESS");
        assertThat(retrieved.getAmount()).isEqualTo(50000);
    }

    @Test
    void shouldReturnNullWhenKeyNotFound() {
        // When
        Invoice invoice = paymentCacheService.getPendingInvoice("NON-EXISTENT");
        TransactionInfo transaction = paymentCacheService.getCompletedPayment("NON-EXISTENT");

        // Then
        assertThat(invoice).isNull();
        assertThat(transaction).isNull();
    }
}
