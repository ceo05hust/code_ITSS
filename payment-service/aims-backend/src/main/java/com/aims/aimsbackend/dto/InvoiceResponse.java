package com.aims.aimsbackend.dto;

import com.aims.aimsbackend.entity.order.Invoice;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class InvoiceResponse {
    private BigDecimal totalProductPriceExclVAT;
    private BigDecimal totalProductPriceInclVAT;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;

    public static InvoiceResponse from(Invoice invoice) {
        InvoiceResponse res = new InvoiceResponse();
        res.totalProductPriceExclVAT = invoice.getTotalProductPriceExclVAT();
        res.totalProductPriceInclVAT = invoice.getTotalProductPriceInclVAT();
        res.shippingFee = invoice.getShippingFee();
        res.totalAmount = invoice.getTotalAmount();
        return res;
    }
}