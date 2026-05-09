package com.aims.payment_service.subsystem.vietqr;

import com.aims.payment_service.entity.Invoice;
import com.aims.payment_service.entity.QRCode;

public interface IPaymentQRCode {

    /**
     * Tạo mã QR thanh toán từ thông tin invoice.
     */
    QRCode generateQRCode(Invoice invoice);

    /**
     * Kiểm tra trạng thái thanh toán qua invoice.
     *
     * @param invoice Hóa đơn cần kiểm tra thanh toán giả lập
     * @return "SUCCESS" | "FAILED" | "PENDING"
     */
    String checkPaymentStatus(Invoice invoice);
}
