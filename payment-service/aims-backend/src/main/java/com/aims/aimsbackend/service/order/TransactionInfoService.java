package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.enums.PaymentMethod;

import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.repository.order.TransactionInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionInfoService {

    @Autowired
    private TransactionInfoRepository transactionInfoRepository;

    public TransactionInfo findByExternalTransactionId(String externalTransactionId) {
        return transactionInfoRepository.findByExternalTransactionId(externalTransactionId).orElse(null);
    }

    public TransactionInfo createTransactionInfo(String externalTransactionId,
                                                 String content,
                                                 BigDecimal amount,
                                                 PaymentMethod paymentMethod) {
        
        // Kiểm tra xem giao dịch đã tồn tại chưa (Idempotent Webhook)
        TransactionInfo existing = findByExternalTransactionId(externalTransactionId);
        if (existing != null) {
            return existing;
        }

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setExternalTransactionId(externalTransactionId);
        transactionInfo.setTransactionContent(content);
        transactionInfo.setAmount(amount);
        transactionInfo.setPaymentMethod(paymentMethod);
        transactionInfo.setTransactionDatetime(LocalDateTime.now());

        return transactionInfoRepository.save(transactionInfo);
    }

}