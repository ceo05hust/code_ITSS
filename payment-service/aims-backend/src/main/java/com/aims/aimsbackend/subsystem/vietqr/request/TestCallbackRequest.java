package com.aims.aimsbackend.subsystem.vietqr.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

/**
 * Request body gửi lên VietQR test-callback API để kích hoạt giả lập thanh toán.
 * VietQR sẽ nhận request này, xử lý rồi gọi lại callback URL (được cấu hình trên VietQR portal).
 *
 * API: POST https://dev.vietqr.org/vqr/bank/api/test/transaction-callback
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestCallbackRequest extends QRRequest {

    /** Số tài khoản nhận tiền (phải khớp cấu hình tạo QR) */
    private String bankAccount;

    /** Nội dung chuyển khoản (phải khớp nội dung lúc tạo QR, ví dụ: "AIMS REF-XXXXXX") */
    private String content;

    /** Số tiền giao dịch */
    private long amount;

    /** Mã ngân hàng nhận tiền */
    private String bankCode;

    /** Loại giao dịch (mặc định "C" - Credit) */
    private String transType;

    @Override
    public String buildRequestString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build TestCallback request: " + e.getMessage(), e);
        }
    }
}
