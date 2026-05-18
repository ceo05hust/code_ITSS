package com.aims.aimsbackend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    VIETQR,
    PAYPAL;

    @JsonCreator
    public static PaymentMethod from(String v) {
        if (v == null) return null;
        String normalized = v.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        switch (normalized) {
            case "VIETQR":
                return VIETQR;
            case "PAYPAL":
                return PAYPAL;
            default:
                throw new IllegalArgumentException("Unknown payment method: " + v);
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
