package com.nikitsya.billing.payment_intent.api;

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

    @PostMapping
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        Customer customer = customerRepository.findById(request.customerId()).orElse(null);
        if (customer == null) {
            return ResponseEntity.notFound().build();
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
