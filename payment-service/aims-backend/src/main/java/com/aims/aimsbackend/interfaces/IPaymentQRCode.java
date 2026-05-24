package com.aims.aimsbackend.interfaces;

import com.aims.aimsbackend.dto.PaymentRequest;
import com.aims.aimsbackend.subsystem.vietqr.response.QRCode;

import java.math.BigDecimal;

public interface IPaymentQRCode {

    QRCode generateQRCode(PaymentRequest request, String externalTransactionId);

    void triggerTestCallback(String externalTransactionId, BigDecimal amount);
}
