package com.nikitsya.billing.subscription.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
import com.nikitsya.billing.price.model.Price;
import com.nikitsya.billing.price.repository.PriceRepository;
import com.nikitsya.billing.subscription.model.Subscription;
import com.nikitsya.billing.subscription.model.SubscriptionStatus;
import com.nikitsya.billing.subscription.repository.SubscriptionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        List<SubscriptionResponse> response = subscriptionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        Optional<Customer> customerOptional = customerRepository.findById(request.customerId());

        if (customerOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "CUSTOMER_NOT_FOUND",
                            "Customer with id " + request.customerId() + " was not found"
                    ));
        }

        Customer customer = customerOptional.get();

        if (subscriptionRepository.existsByCustomer_Id(customer.getId())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            "CUSTOMER_ALREADY_HAS_SUBSCRIPTION",
                            "Customer with id " + customer.getId() + " already has a subscription"
                    ));
        }

        Optional<Price> priceOptional = priceRepository.findById(request.priceId());

        if (priceOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "PRICE_NOT_FOUND",
                            "Price with id " + request.priceId() + " was not found"
                    ));
        }

        Price price = priceOptional.get();

        if (!isValidBillingInterval(price.getBillingInterval())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "INVALID_BILLING_INTERVAL",
                            "Only MONTHLY and YEARLY prices can be used for subscriptions"
                    ));
        }

        LocalDateTime periodStart = LocalDateTime.now();
        LocalDateTime periodEnd = calculatePeriodEnd(periodStart, price);

        Subscription subscription = new Subscription(
                customer,
                price,
                SubscriptionStatus.ACTIVE,
                periodStart,
                periodEnd
        );

        Subscription saved = subscriptionRepository.save(subscription);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(saved));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable Long id) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(id);

        if (subscriptionOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "SUBSCRIPTION_NOT_FOUND",
                            "Subscription with id " + id + " was not found"
                    ));
        }

        Subscription subscription = subscriptionOptional.get();

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            "SUBSCRIPTION_ALREADY_CANCELED",
                            "Subscription with id " + id + " is already canceled"
                    ));
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);

        Subscription saved = subscriptionRepository.save(subscription);

        return ResponseEntity.ok(toResponse(saved));
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getCustomer().getId(),
                subscription.getPrice().getId(),
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd()
        );
    }

    private boolean isValidBillingInterval(String billingInterval) {
        return "MONTHLY".equals(billingInterval) || "YEARLY".equals(billingInterval);
    }

    private LocalDateTime calculatePeriodEnd(LocalDateTime start, Price price) {
        return switch (price.getBillingInterval()) {
            case "MONTHLY" -> start.plusMonths(1);
            case "YEARLY" -> start.plusYears(1);
            default -> throw new IllegalArgumentException("Invalid billing interval");
        };
    }
}
