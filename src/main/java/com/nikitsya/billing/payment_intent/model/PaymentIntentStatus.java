package com.nikitsya.billing.payment_intent.model;

public enum PaymentIntentStatus {
    REQUIRES_CONFIRMATION,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
