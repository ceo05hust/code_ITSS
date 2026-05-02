package com.aims.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoiceId")
    private Integer invoiceId;

    @Column(name = "shippingFee")
    private double shippingFee;

    @Column(name = "totalProductPriceExclVAT")
    private double totalProductPriceExVAT;

    @Column(name = "totalProductPriceInclVAT")
    private double totalProductPriceIncVAT;

    @Column(name = "totalAmount")
    private double totalAmount;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "transactionId", referencedColumnName = "transactionId")
    private TransactionInfo transactionInfo;

    /** Lưu mã VQRxxxxx của VietQR để dùng khi trigger test callback */
    @Column(name = "vietQrTransactionId")
    private String vietQrTransactionId;

    public void markAsPaid(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public void updateInvoice(double newShippingFee) {
        this.shippingFee = newShippingFee;
        this.totalAmount = this.totalProductPriceIncVAT + newShippingFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
