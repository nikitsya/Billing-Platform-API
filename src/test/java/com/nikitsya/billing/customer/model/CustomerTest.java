package com.nikitsya.billing.customer.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void onCreate_whenCreatedAtIsNull_setsCreatedAt() {
        Customer customer =
                new Customer("Hanna K", "hanna_k@gmail.com");

        assertNull(customer.getCreatedAt());

        customer.onCreate();

        assertNotNull(customer.getCreatedAt());
    }

    @Test
    void onCreate_whenCreatedAtAlreadyExists_doesNotChangeIt() {
        Customer customer =
                new Customer("Hanna K", "hanna_k@gmail.com");

        LocalDateTime existingCreatedAt = LocalDateTime.of(2026, 8, 8, 12, 0);

        ReflectionTestUtils.setField(
                customer,
                "createdAt",
                existingCreatedAt
        );

        customer.onCreate();

        assertEquals(existingCreatedAt, customer.getCreatedAt());
    }
}
