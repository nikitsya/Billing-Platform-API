package com.nikitsya.billing.price.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.price.model.Price;
import com.nikitsya.billing.price.repository.PriceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final PriceRepository priceRepository;

    public PriceController(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @GetMapping
    public ResponseEntity<List<PriceResponse>> getAllPrices() {
        List<PriceResponse> response = priceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPrice(@PathVariable Long id) {
        Optional<Price> priceOptional = priceRepository.findById(id);

        if (priceOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "PRICE_NOT_FOUND",
                            "Price with id " + id + " was not found"
                    ));
        }

        return ResponseEntity.ok(toResponse(priceOptional.get()));
    }

    private PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.getId(),
                price.getProduct().getId(),
                price.getUnitAmountCents(),
                price.getCurrency(),
                price.getBillingInterval(),
                price.getActive(),
                price.getCreatedAt()
        );
    }
}
