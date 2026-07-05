package com.nikitsya.billing.payment_intent.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record CreatePaymentIntentRequest(
        @Positive @NotNull Long amount,
        @NotBlank String currency,
        @NotNull Long customerId,
        String description,
        Map<String, Object> metadata
) {
}
