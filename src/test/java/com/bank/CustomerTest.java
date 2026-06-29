package com.bank;

import com.bank.model.Customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Customer model.
 *
 * These tests check simple model behavior: constructor values, default account
 * list, email update, and the role returned by getRole().
 */
class CustomerTest {

    @Test
    void customerConstructor_shouldInitializeFields() {

        // Arrange + Act: create a customer with known values.
        Customer customer =
                new Customer(
                        "C001",
                        "Ahmed",
                        "ahmed01",
                        "1234",
                        "ahmed@gmail.com"
                );

        // Assert: each getter should return the value passed to the constructor.
        assertEquals(
                "C001",
                customer.getId()
        );

        assertEquals(
                "Ahmed",
                customer.getFullName()
        );

        assertEquals(
                "ahmed01",
                customer.getUsername()
        );

        assertEquals(
                "1234",
                customer.getPassword()
        );

        assertEquals(
                "ahmed@gmail.com",
                customer.getEmail()
        );
    }

    @Test
    void newCustomer_shouldHaveEmptyAccountList() {

        Customer customer =
                new Customer(
                        "C002",
                        "Sara",
                        "sara01",
                        "pass",
                        "sara@gmail.com"
                );

        // A new customer should start with an empty account list.
        assertNotNull(
                customer.getAccounts()
        );

        assertTrue(
                customer.getAccounts().isEmpty()
        );
    }

    @Test
    void setEmail_shouldUpdateEmail() {

        Customer customer =
                new Customer(
                        "C003",
                        "Ali",
                        "ali01",
                        "123",
                        "old@gmail.com"
                );

        customer.setEmail(
                "new@gmail.com"
        );

        assertEquals(
                "new@gmail.com",
                customer.getEmail()
        );
    }

    @Test
    void customerRole_shouldReturnCustomer() {

        Customer customer =
                new Customer(
                        "C004",
                        "Omar",
                        "omar01",
                        "123",
                        "omar@gmail.com"
                );

        assertEquals(
                "CUSTOMER",
                customer.getRole()
        );
    }

}
