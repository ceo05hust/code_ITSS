package com.aims.aimsbackend.config;

import com.aims.aimsbackend.dto.ApiResponse;
import com.aims.aimsbackend.exception.order.*;
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

    @ExceptionHandler(CallbackValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleCallbackValidation(CallbackValidationException e) {
        log.error("CallbackValidationException: {}", e.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentFailed(PaymentFailedException e) {
        log.warn("PaymentFailedException: {}", e.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(PaymentTimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleTimeout(PaymentTimeoutException e) {
        log.warn("PaymentTimeoutException: {}", e.getMessage());
        return ResponseEntity.status(408).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnknown(UnknownException e) {
        log.error("UnknownException: {}", e.getMessage());
        return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(ApiResponse.error("Internal server error: " + e.getMessage()));
    }
}
