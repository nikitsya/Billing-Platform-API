package com.nikitsya.billing.customer.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void getCustomer_whenCustomerDoesNotExist_returnsNotFound() {
        Long id = 1L;

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = customerController.getCustomer(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getBody());

        assertEquals("CUSTOMER_NOT_FOUND", body.error());
        assertEquals("Customer with id " + id + " was not found", body.message());

        verify(customerRepository).findById(id);
    }

    @Test
    void getCustomer_whenCustomerExists_returnsOk() {
        Long id = 1L;

        when(customerRepository.findById(id)).thenReturn(
                Optional.of(new Customer("Hanna K", "hanna_k@gmail.com")));

        ResponseEntity<?> response = customerController.getCustomer(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        CustomerResponse body = assertInstanceOf(CustomerResponse.class, response.getBody());

        assertEquals("Hanna K", body.name());
        assertEquals("hanna_k@gmail.com", body.email());

        verify(customerRepository).findById(id);
    }

    @Test
    void getAllCustomers_whenCustomersExist_returnsCustomers() {
        when(customerRepository.findAll()).thenReturn(
                List.of(
                        new Customer("Hanna K", "hanna_k@gmail.com"),
                        new Customer("John Doe", "john@gmail.com")
                ));

        ResponseEntity<List<CustomerResponse>> response = customerController.getAllCustomers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<CustomerResponse> body = response.getBody();

        assertEquals(2, body.size());

        assertEquals("Hanna K", body.getFirst().name());
        assertEquals("hanna_k@gmail.com", body.getFirst().email());

        assertEquals("John Doe", body.getLast().name());
        assertEquals("john@gmail.com", body.getLast().email());

        verify(customerRepository).findAll();
    }

    @Test
    void getAllCustomers_whenNoCustomersExist_returnsEmptyList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<CustomerResponse>> response = customerController.getAllCustomers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<CustomerResponse> body = response.getBody();
        assertTrue(body.isEmpty());

        verify(customerRepository).findAll();
    }

    @Test
    void createCustomer_whenEmailAlreadyExists_returnsConflict() {
        CreateCustomerRequest request = new CreateCustomerRequest("Hanna K", "hanna_k@gmail.com");

        when(customerRepository.existsByEmail("hanna_k@gmail.com")).thenReturn(true);

        ResponseEntity<?> response = customerController.createCustomer(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getBody());

        assertEquals("EMAIL_ALREADY_EXISTS", body.error());
        assertEquals("Customer with email hanna_k@gmail.com already exists", body.message());

        verify(customerRepository).existsByEmail("hanna_k@gmail.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_whenEmailDoesNotExist_returnsCreated() {
        CreateCustomerRequest request = new CreateCustomerRequest("Hanna K", "  HANNA_K@GMAIL.COM  ");
        Customer savedCustomer = new Customer("Hanna K", "hanna_k@gmail.com");

        when(customerRepository.existsByEmail("hanna_k@gmail.com")).thenReturn(false);

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        ResponseEntity<?> response = customerController.createCustomer(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        CustomerResponse body = assertInstanceOf(CustomerResponse.class, response.getBody());

        assertEquals("Hanna K", body.name());
        assertEquals("hanna_k@gmail.com", body.email());

        verify(customerRepository).existsByEmail("hanna_k@gmail.com");
        verify(customerRepository).save(argThat(customer ->
                customer.getName().equals("Hanna K") && customer.getEmail().equals("hanna_k@gmail.com")
        ));
    }
}