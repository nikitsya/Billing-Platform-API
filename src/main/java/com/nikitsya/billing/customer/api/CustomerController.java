package com.nikitsya.billing.customer.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
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
@RequestMapping(path = "/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final PaymentIntentRepository paymentIntentRepository;

    public CustomerController(CustomerRepository customerRepository,
                              PaymentIntentRepository paymentIntentRepository) {
        this.customerRepository = customerRepository;
        this.paymentIntentRepository = paymentIntentRepository;
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (customerRepository.existsByEmail(email)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)   // 409
                    .body(new ErrorResponse(
                            "EMAIL_ALREADY_EXISTS",
                            "Customer with email " + email + " already exists"
                    ));
        }
        Customer saved = customerRepository.save(
                new Customer(request.name(), email)
        );
        CustomerResponse response = new CustomerResponse(saved.getId(), saved.getName(), saved.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            CustomerResponse response = new CustomerResponse(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail()
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "CUSTOMER_NOT_FOUND",
                        "Customer with id " + id + " was not found"
                ));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerResponse> response = new ArrayList<>();
        for (Customer customer : customers) {
            response.add(new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail()));
        }
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);

        if (customerOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "CUSTOMER_NOT_FOUND",
                            "Customer with id " + id + " was not found"
                    ));
        }

        if (paymentIntentRepository.existsByCustomer_Id(id)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            "CUSTOMER_HAS_PAYMENT_INTENTS",
                            "Customer with id " + id + " cannot be deleted because they have payment intents"
                    ));
        }

        customerRepository.delete(customerOptional.get());

        return ResponseEntity.noContent().build();
    }
}
