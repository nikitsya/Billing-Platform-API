package com.nikitsya.billing.subscription.api;

import com.nikitsya.billing.subscription.model.SubscriptionStatus;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        Long customerId,
        Long priceId,
        SubscriptionStatus status,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd
) {
}
