package com.nikitsya.billing.common.api;

public record ErrorResponse(
        String error,
        String message
) {
}
