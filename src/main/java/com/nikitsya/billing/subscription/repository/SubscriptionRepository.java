package com.nikitsya.billing.subscription.repository;

import com.nikitsya.billing.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByCustomer_Id(Long customerId);
}
