package com.aims.aimsbackend.entity.order;

import com.aims.aimsbackend.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    private Long transactionId;

    @Column(nullable = false, length = 255, unique = true)
    private String externalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String transactionContent;

    @Column(nullable = false)
    private LocalDateTime transactionDatetime;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @OneToOne(mappedBy = "transactionInfo")
    private Invoice invoice;

    /**
     * Factory method để tạo TransactionInfo sau khi VietQR callback xác nhận thành công.
     * externalTransactionId: ID giao dịch từ VietQR (payload.getTransactionId())
     */
    public static TransactionInfo createTransactionInfo(
            String externalTransactionId,
            String transactionContent,
            Invoice invoice,
            BigDecimal amount,
            PaymentMethod paymentMethod
    ) {
        return TransactionInfo.builder()
                .externalTransactionId(externalTransactionId)
                .transactionContent(transactionContent)
                .transactionDatetime(LocalDateTime.now())
                .amount(amount)
                .paymentMethod(paymentMethod)
                .build();
    }
}