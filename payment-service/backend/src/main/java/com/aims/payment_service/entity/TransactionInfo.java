package com.aims.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"TransactionInfo\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"transactionId\"")
    private Integer transactionId;

    @OneToOne
    @JoinColumn(name = "\"invoiceId\"", referencedColumnName = "\"invoiceId\"")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"paymentMethod\"")
    private PaymentMethod paymentMethod;

    @Column(name = "\"transactionContent\"", length = 500)
    private String transactionContent;

    @Column(name = "\"transactionDatetime\"")
    private LocalDateTime transactionDateTime;

    @Column(name = "\"amount\"")
    private double amount;

    /** "SUCCESS" | "FAILED" | "PENDING" */
    @Column(name = "\"status\"")
    private String status;

    public static TransactionInfo createTransactionInfo(
            String vietQrTransactionId,
            String transactionContent,
            Invoice invoice,
            double amount,
            PaymentMethod paymentMethod
    ) {
        String fullContent = "VietQR Ref: " + vietQrTransactionId + " | " + transactionContent;
        return TransactionInfo.builder()
                .invoice(invoice)
                .paymentMethod(paymentMethod)
                .transactionContent(fullContent)
                .transactionDateTime(LocalDateTime.now())
                .amount(amount)
                .status("SUCCESS")
                .build();
    }

}
