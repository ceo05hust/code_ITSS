package com.aims.aimsbackend.subsystem.vietqr;

import com.aims.aimsbackend.dto.InvoiceResponse;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;

import java.math.BigDecimal;

public interface IPaymentQRCode {

    QRCode generateQRCode(InvoiceResponse response, String externalTransactionId);

    void triggerTestCallback(String externalTransactionId, BigDecimal amount);
}
