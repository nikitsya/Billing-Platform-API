package com.nikitsya.billing.customer.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
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

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            CustomerResponse response = new CustomerResponse(customer.get().getId(), customer.get().getName(), customer.get().getEmail());
            return ResponseEntity.ok().body(response);
        }
        return ResponseEntity.notFound().build();
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
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty()) return ResponseEntity.notFound().build();
        customerRepository.delete(customer.get());
        return ResponseEntity.noContent().build();
    }
}
