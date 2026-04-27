package com.aims.payment_service.subsystem.vietqr.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class cho các VietQR HTTP request.
 * Theo class diagram: QRRequest chứa CONTENT_TYPE và AUTHORIZATION header constants.
 */
@Getter
@Setter
public abstract class QRRequest {

    protected static final String CONTENT_TYPE  = "application/json";
    protected static final String AUTHORIZATION = "Authorization";

    /**
     * Serialize request thành JSON string để gửi HTTP body.
     */
    public abstract String buildRequestString();
}
