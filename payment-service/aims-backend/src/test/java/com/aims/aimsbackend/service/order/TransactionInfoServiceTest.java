package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.enums.PaymentMethod;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.repository.order.TransactionInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionInfoServiceTest {

    @Mock
    private TransactionInfoRepository repository;

    @InjectMocks
    private TransactionInfoService service;

    private String testExternalId;
    private TransactionInfo mockTransaction;

    @BeforeEach
    void setUp() {
        testExternalId = "REF-TEST1234";
        mockTransaction = new TransactionInfo();
        mockTransaction.setTransactionId(1L);
        mockTransaction.setExternalTransactionId(testExternalId);
        mockTransaction.setAmount(new BigDecimal("500000"));
        mockTransaction.setPaymentMethod(PaymentMethod.VIETQR);
    }

    @Test
    void testCreateTransactionInfo() {
        when(repository.save(any(TransactionInfo.class))).thenReturn(mockTransaction);

        TransactionInfo result = service.createTransactionInfo(
                testExternalId,
                "AIMS " + testExternalId,
                new BigDecimal("500000"),
                PaymentMethod.VIETQR
        );

        assertNotNull(result);
        assertEquals(testExternalId, result.getExternalTransactionId());
        assertEquals(new BigDecimal("500000"), result.getAmount());
        assertEquals(PaymentMethod.VIETQR, result.getPaymentMethod());

        verify(repository, times(1)).save(any(TransactionInfo.class));
    }

    @Test
    void testFindByExternalTransactionId_Found() {
        when(repository.findByExternalTransactionId(testExternalId))
                .thenReturn(Optional.of(mockTransaction));

        TransactionInfo result = service.findByExternalTransactionId(testExternalId);

        assertNotNull(result);
        assertEquals(testExternalId, result.getExternalTransactionId());
    }

    @Test
    void testFindByExternalTransactionId_NotFound() {
        when(repository.findByExternalTransactionId("UNKNOWN"))
                .thenReturn(Optional.empty());

        TransactionInfo result = service.findByExternalTransactionId("UNKNOWN");

        assertNull(result);
    }
}
