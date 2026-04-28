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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "transaction_content", length = 500)
    private String transactionContent;

    @Column(name = "transaction_date_time")
    private LocalDateTime transactionDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    /** invoiceId của Invoice liên kết */
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "amount")
    private double amount;

    /** "SUCCESS" | "FAILED" | "PENDING" */
    @Column(name = "status")
    private String status;

    public static TransactionInfo createTransactionInfo(
            String vietQrTransactionId,
            String transactionContent,
            Integer invoiceId,
            double amount,
            PaymentMethod paymentMethod
    ) {
        String fullContent = "VietQR Ref: " + vietQrTransactionId + " | " + transactionContent;
        return TransactionInfo.builder()
                .transactionContent(fullContent)
                .transactionDateTime(LocalDateTime.now())
                .paymentMethod(paymentMethod)
                .orderId(invoiceId)
                .amount(amount)
                .status("SUCCESS")
                .build();
    }

    public Integer getTransactionId() { return transactionId; }
    public String getStatus()        { return status; }
}
