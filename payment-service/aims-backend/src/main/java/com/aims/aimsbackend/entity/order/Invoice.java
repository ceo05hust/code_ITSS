package com.aims.aimsbackend.entity.order;

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
    private Integer invoiceId;

    private double totalProductPriceExVAT;

    private double totalProductPriceIncVAT;

    private double shippingFee;

    private double totalAmount;

    // Quan hệ 1-1, TransactionInfo là bên giữ khóa ngoại (invoice_id)
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TransactionInfo transactionInfo;

    // Field phụ phục vụ logic thanh toán
    private String paymentReference;

    /** Dùng khi VietQR callback xác nhận thanh toán thành công */
    public void markAsPaid(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }
}
