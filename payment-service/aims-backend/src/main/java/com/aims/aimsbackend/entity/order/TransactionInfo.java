package com.aims.aimsbackend.entity.order;

import com.aims.aimsbackend.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_info")
@Data
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

}