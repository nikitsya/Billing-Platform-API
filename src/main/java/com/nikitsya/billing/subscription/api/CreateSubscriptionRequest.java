package com.nikitsya.billing.subscription.api;

import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull Long customerId,
        @NotNull Long priceId
) {
}
