package com.nikitsya.billing.subscription.repository;

import com.nikitsya.billing.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByCustomer_Id(Long customerId);
}
