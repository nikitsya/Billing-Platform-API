package com.nikitsya.billing.payment_intent.api;

import com.nikitsya.billing.payment_intent.model.PaymentIntent;
import com.nikitsya.billing.payment_intent.repository.PaymentIntentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment_intents")
public class PaymentIntentController {

    private final PaymentIntentRepository paymentIntentRepository;

    public PaymentIntentController(PaymentIntentRepository paymentIntentRepository) {
        this.paymentIntentRepository = paymentIntentRepository;
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
