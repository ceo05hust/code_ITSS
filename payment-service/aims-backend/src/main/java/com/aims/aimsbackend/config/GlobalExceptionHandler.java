package com.aims.aimsbackend.config;

import com.aims.aimsbackend.dto.ApiResponse;
import com.aims.aimsbackend.exception.order.InvalidTokenException;
import com.aims.aimsbackend.exception.order.QRCodeGenerationException;
import com.aims.aimsbackend.exception.order.UnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException e) {
        log.error("InvalidTokenException: {}", e.getMessage());
        return ResponseEntity.status(503).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(QRCodeGenerationException.class)
    public ResponseEntity<ApiResponse<Object>> handleQRGeneration(QRCodeGenerationException e) {
        log.error("QRCodeGenerationException: {}", e.getMessage());
        return ResponseEntity.status(502).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnknown(UnknownException e) {
        log.error("UnknownException: {}", e.getMessage());
        return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(com.aims.aimsbackend.exception.order.PaymentFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentFailed(com.aims.aimsbackend.exception.order.PaymentFailedException e) {
        log.error("PaymentFailedException: {}", e.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(com.aims.aimsbackend.exception.order.PaymentTimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentTimeout(com.aims.aimsbackend.exception.order.PaymentTimeoutException e) {
        log.error("PaymentTimeoutException: {}", e.getMessage());
        return ResponseEntity.status(408).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(com.aims.aimsbackend.exception.order.CallbackValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleCallbackValidation(com.aims.aimsbackend.exception.order.CallbackValidationException e) {
        log.error("CallbackValidationException: {}", e.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(ApiResponse.error("Internal server error: " + e.getMessage()));
    }
}
