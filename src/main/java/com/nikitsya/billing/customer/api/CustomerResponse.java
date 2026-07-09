package com.nikitsya.billing.customer.api;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}
