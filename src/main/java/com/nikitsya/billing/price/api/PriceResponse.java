package com.nikitsya.billing.price.api;

import java.time.LocalDateTime;

public record PriceResponse(
        Long id,
        Long productId,
        Integer unitAmountCents,
        String currency,
        String billingInterval,
        Boolean active,
        LocalDateTime createdAt
) {
}
