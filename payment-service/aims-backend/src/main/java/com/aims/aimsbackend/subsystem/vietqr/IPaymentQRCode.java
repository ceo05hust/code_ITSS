package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.entity.order.QRCode;

import java.math.BigDecimal;

public interface IPaymentQRCode {

    /**
     * Tạo mã QR thanh toán từ thông tin đơn hàng và mã tham chiếu giao dịch.
     *
     * @param response              Thông tin đơn hàng phản hồi từ hệ thống (số tiền, phí ship, v.v.)
     * @param externalTransactionId Mã tham chiếu giao dịch duy nhất (ví dụ: REF-A1B2C3D4)
     * @return QRCode               Đối tượng chứa URL ảnh QR và dữ liệu QR
     */
    QRCode generateQRCode(InvoiceResponse response, String externalTransactionId);

    /**
     * Kích hoạt giả lập thanh toán thành công (dành cho môi trường test).
     *
     * @param externalTransactionId Mã tham chiếu giao dịch đã dùng để tạo QR
     * @param amount                Số tiền giao dịch cần giả lập
     */
    void triggerTestCallback(String externalTransactionId, BigDecimal amount);
}
