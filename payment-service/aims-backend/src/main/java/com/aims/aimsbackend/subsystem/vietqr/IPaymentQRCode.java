package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.QRCode;

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
