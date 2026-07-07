package com.nikitsya.billing.product.api;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt
) {
}
