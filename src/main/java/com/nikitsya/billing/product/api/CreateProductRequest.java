package com.nikitsya.billing.product.api;

import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
        @NotBlank String name,
        String description
) {
}
