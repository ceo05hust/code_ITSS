package com.aims.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionInfo {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "transaction_content", length = 500)
    private String transactionContent;

    @Column(name = "transaction_date_time")
    private LocalDateTime transactionDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    /** invoiceId của Invoice liên kết */
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "amount")
    private double amount;

    /** "SUCCESS" | "FAILED" | "PENDING" */
    @Column(name = "status")
    private String status;

    public static TransactionInfo createTransactionInfo(
            String transactionId,
            String transactionContent,
            String invoiceId,
            double amount,
            PaymentMethod paymentMethod
    ) {
        return TransactionInfo.builder()
                .transactionId(transactionId)
                .transactionContent(transactionContent)
                .transactionDateTime(LocalDateTime.now())
                .paymentMethod(paymentMethod)
                .orderId(invoiceId)
                .amount(amount)
                .status("SUCCESS")
                .build();
    }

    public String getTransactionId() { return transactionId; }
    public String getStatus()        { return status; }
}
