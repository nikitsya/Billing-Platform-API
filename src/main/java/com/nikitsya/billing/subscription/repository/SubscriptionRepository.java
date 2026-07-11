package com.nikitsya.billing.subscription.repository;

import com.nikitsya.billing.subscription.model.Subscription;
import com.nikitsya.billing.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByCustomer_IdAndStatusNot(Long id, SubscriptionStatus subscriptionStatus);
}
