package com.nikitsya.billing.payment_intent.api;

import com.nikitsya.billing.payment_intent.model.PaymentIntentStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record PaymentIntentResponse(
        Long id,
        Long amount,
        String currency,
        PaymentIntentStatus status,
        Long customerId,
        String description,
        Map<String, Object> metadata,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
