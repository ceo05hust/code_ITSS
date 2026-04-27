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
    @Column(name = "invoice_id")
    private String invoiceId;

    @Column(name = "shipping_fee")
    private double shippingFee;

    @Column(name = "total_product_price_ex_vat")
    private double totalProductPriceExVAT;

    @Column(name = "total_product_price_inc_vat")
    private double totalProductPriceIncVAT;

    @Column(name = "total_amount")
    private double totalAmount;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
    private TransactionInfo transactionInfo;

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
