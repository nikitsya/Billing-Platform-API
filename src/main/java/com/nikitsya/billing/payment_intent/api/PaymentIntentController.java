package com.nikitsya.billing.payment_intent.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
import com.nikitsya.billing.payment_intent.model.PaymentIntent;
import com.nikitsya.billing.payment_intent.model.PaymentIntentStatus;
import com.nikitsya.billing.payment_intent.repository.PaymentIntentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment_intents")
public class PaymentIntentController {

    private final PaymentIntentRepository paymentIntentRepository;
    private final CustomerRepository customerRepository;

    public PaymentIntentController(PaymentIntentRepository paymentIntentRepository,
                                   CustomerRepository customerRepository) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public ResponseEntity<List<PaymentIntentResponse>> getPaymentIntents() {
        List<PaymentIntent> paymentIntents = paymentIntentRepository.findAll();
        List<PaymentIntentResponse> response = new ArrayList<>();
        for (PaymentIntent paymentIntent : paymentIntents) {
            response.add(toResponse(paymentIntent));
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentIntentById(@PathVariable Long id) {
        Optional<PaymentIntent> paymentIntentOptional = paymentIntentRepository.findById(id);

        if (paymentIntentOptional.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOptional.get();
            return ResponseEntity.ok().body(toResponse(paymentIntent));
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent with id " + id + " was not found"
                ));
    }

    @PostMapping
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        Customer customer = customerRepository.findById(request.customerId()).orElse(null);
        if (customer == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "CUSTOMER_NOT_FOUND",
                            "Customer with id " + request.customerId() + " was not found"
                    ));
        }

        PaymentIntent saved = paymentIntentRepository.save(
                new PaymentIntent(
                        request.amount(),
                        request.currency().toLowerCase(Locale.ROOT),
                        PaymentIntentStatus.REQUIRES_CONFIRMATION,
                        customer,
                        request.description(),
                        request.metadata(),
                        null,
                        null
                )
        );
        PaymentIntentResponse response = toResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmPaymentIntent(@PathVariable Long id) {
        Optional<PaymentIntent> paymentIntentOptional = paymentIntentRepository.findById(id);

        if (paymentIntentOptional.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOptional.get();

            if (paymentIntent.getStatus() != PaymentIntentStatus.REQUIRES_CONFIRMATION) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                "PAYMENT_INTENT_CANNOT_BE_CONFIRMED",
                                "Payment intent cannot be confirmed while in status " + paymentIntent.getStatus()
                        ));
            }

            paymentIntent.setStatus(PaymentIntentStatus.PROCESSING);

            PaymentIntent saved = paymentIntentRepository.save(paymentIntent);

            return ResponseEntity.ok(toResponse(saved));
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent with id " + id + " was not found"
                ));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelPaymentIntent(@PathVariable Long id) {
        Optional<PaymentIntent> paymentIntentOptional = paymentIntentRepository.findById(id);

        if (paymentIntentOptional.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOptional.get();

            if (paymentIntent.getStatus() != PaymentIntentStatus.REQUIRES_CONFIRMATION) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                "PAYMENT_INTENT_CANNOT_BE_CANCELED",
                                "Payment intent cannot be canceled while in status " + paymentIntent.getStatus()
                        ));
            }

            paymentIntent.setStatus(PaymentIntentStatus.CANCELLED);

            PaymentIntent saved = paymentIntentRepository.save(paymentIntent);

            return ResponseEntity.ok(toResponse(saved));
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent with id " + id + " was not found"
                ));
    }

    private PaymentIntentResponse toResponse(PaymentIntent paymentIntent) {
        return new PaymentIntentResponse(
                paymentIntent.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                paymentIntent.getStatus(),
                paymentIntent.getCustomer().getId(),
                paymentIntent.getDescription(),
                paymentIntent.getMetadata(),
                paymentIntent.getCreatedAt(),
                paymentIntent.getUpdatedAt()
        );
    }
}
