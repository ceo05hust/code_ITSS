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
    @Column(name = "\"paymentReference\"")
    private String paymentReference;

    /** Dùng khi VietQR callback xác nhận thanh toán thành công */
    public void markAsPaid(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }
}
