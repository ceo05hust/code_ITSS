package com.aims.aimsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Request từ Angular frontend để tạo QR thanh toán.
 */
@Data
public class InvoiceRequest {
    private Long invoiceId;
    private BigDecimal shippingFee;
    private BigDecimal totalProductPriceExclVAT;
    private BigDecimal totalProductPriceInclVAT;
    private BigDecimal totalAmount;
}
