package com.aims.aimsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO cho request giả lập thanh toán thành công.
 * Client gửi lên POST /api/payment/simulate-payment sau khi đã có mã QR.
 */
@Data
public class SimulatePaymentRequest {

    /** Mã tham chiếu giao dịch duy nhất — khớp với externalTransactionId từ /generate-qr */
    @NotBlank(message = "externalTransactionId không được để trống")
    private String externalTransactionId;

    /** Tổng số tiền cần giả lập thanh toán */
    @NotNull(message = "amount không được để trống")
    @Positive(message = "amount phải lớn hơn 0")
    private BigDecimal amount;
}
