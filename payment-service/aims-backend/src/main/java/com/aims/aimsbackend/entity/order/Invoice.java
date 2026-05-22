package com.aims.aimsbackend.entity.order;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = true)
    private TransactionInfo transactionInfo;

    @Column(name = "total_product_price_excl_vat", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProductPriceExclVAT;

    @Column(name = "total_product_price_incl_vat", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProductPriceInclVAT;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToOne(mappedBy = "invoice")
    private Order order;
}