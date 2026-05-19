package com.aims.aimsbackend.entity.order;

import jakarta.persistence.*;
import lombok.*;
import com.aims.aimsbackend.entity.enums.PaymentMethod;

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
    private Integer transactionId;

    @OneToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String transactionContent;

    private LocalDateTime transactionDateTime;

    private double amount;

    /** "SUCCESS" | "FAILED" | "PENDING" */
    private String status;

    public static TransactionInfo createTransactionInfo(
            String paymentReference,
            String transactionContent,
            Invoice invoice,
            double amount,
            PaymentMethod paymentMethod
    ) {
        String fullContent = "Payment Ref: " + paymentReference + " | " + transactionContent;
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
