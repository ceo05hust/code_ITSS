package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentCacheService {

    // Lưu Invoice tạm thời (chưa lưu vào DB) với key là paymentRef
    private final Map<String, Invoice> pendingInvoices = new ConcurrentHashMap<>();

    // Lưu kết quả giao dịch (đã lưu DB) với key là paymentRef để Frontend kiểm tra
    private final Map<String, TransactionInfo> completedPayments = new ConcurrentHashMap<>();

    public void addPendingInvoice(String paymentRef, Invoice invoice) {
        pendingInvoices.put(paymentRef, invoice);
    }

    public Invoice getPendingInvoice(String paymentRef) {
        return pendingInvoices.get(paymentRef);
    }

    public void removePendingInvoice(String paymentRef) {
        pendingInvoices.remove(paymentRef);
    }

    public void addCompletedPayment(String paymentRef, TransactionInfo transactionInfo) {
        completedPayments.put(paymentRef, transactionInfo);
    }

    public TransactionInfo getCompletedPayment(String paymentRef) {
        return completedPayments.get(paymentRef);
    }
}
