package com.nikitsya.billing.customer.api;

import com.nikitsya.billing.common.api.ErrorResponse;
import com.nikitsya.billing.customer.model.Customer;
import com.nikitsya.billing.customer.repository.CustomerRepository;
import com.nikitsya.billing.payment_intent.repository.PaymentIntentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentIntentRepository paymentIntentRepository;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void getCustomer_whenCustomerDoesNotExist_returnsNotFound() {

        Long id = 1L;

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = customerController.getCustomer(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());

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
}