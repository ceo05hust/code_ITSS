package com.aims.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"Invoice\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"invoiceId\"")
    private Integer invoiceId;

    @Column(name = "\"totalProductPriceExclVAT\"")
    private double totalProductPriceExVAT;

    @Column(name = "\"totalProductPriceInclVAT\"")
    private double totalProductPriceIncVAT;

    @Column(name = "\"shippingFee\"")
    private double shippingFee;

    @Column(name = "\"totalAmount\"")
    private double totalAmount;

    // Quan hệ 1-1, TransactionInfo là bên giữ khóa ngoại (invoiceId)
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TransactionInfo transactionInfo;

    // Field phụ phục vụ logic thanh toán (không nằm trong yêu cầu chuẩn của nhóm nhưng cần thiết cho app)
    @Column(name = "\"vietQrTransactionId\"")
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
