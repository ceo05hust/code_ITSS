package com.aims.aimsbackend.dto;

import lombok.Data;

/**
 * Request từ Angular frontend để tạo QR hoặc xác nhận thanh toán.
 */
@Data
public class InvoiceRequest {
    private Integer invoiceId;
    private double shippingFee;
    private double totalProductPriceExVAT;
    private double totalProductPriceIncVAT;
    private double totalAmount;
}
