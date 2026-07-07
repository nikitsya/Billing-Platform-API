package com.nikitsya.billing.payment_intent.repository;

import com.nikitsya.billing.payment_intent.model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

    boolean existsByCustomer_Id(Long customerId);
}
