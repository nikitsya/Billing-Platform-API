package com.nikitsya.billing.subscription.api;

import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
import com.nikitsya.billing.price.model.Price;
import com.nikitsya.billing.price.repository.PriceRepository;
import com.nikitsya.billing.subscription.model.Subscription;
import com.nikitsya.billing.subscription.model.SubscriptionStatus;
import com.nikitsya.billing.subscription.repository.SubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PriceRepository priceRepository;

    public SubscriptionController(
            SubscriptionRepository subscriptionRepository,
            CustomerRepository customerRepository,
            PriceRepository priceRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
        this.priceRepository = priceRepository;
    }

    @GetMapping
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @PostMapping
    public Subscription createSubscription(@RequestBody CreateSubscriptionRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));

        if (subscriptionRepository.existsByCustomer_Id(customer.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer already has a subscription"
            );
        }

        Price price = priceRepository.findById(request.priceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Price not found"
                ));

        LocalDateTime periodStart = LocalDateTime.now();
        LocalDateTime periodEnd = calculatePeriodEnd(periodStart, price);

        Subscription subscription = new Subscription(
                customer,
                price,
                SubscriptionStatus.ACTIVE,
                periodStart,
                periodEnd
        );

        return subscriptionRepository.save(subscription);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        Optional<Subscription> subscription = subscriptionRepository.findById(id);
        if (subscription.isEmpty()) return ResponseEntity.notFound().build();

        subscriptionRepository.delete(subscription.get());
        return ResponseEntity.noContent().build();
    }

    private LocalDateTime calculatePeriodEnd(LocalDateTime start, Price price) {
        String billingInterval = price.getBillingInterval();

        return switch (billingInterval) {
            case "MONTHLY" -> start.plusMonths(1);
            case "YEARLY" -> start.plusYears(1);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only MONTHLY and YEARLY prices can be used for subscriptions"
            );
        };
    }

    public record CreateSubscriptionRequest(
            Long customerId,
            Long priceId
    ) {
    }
}
