package com.bank;

import com.bank.model.Customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void customerConstructor_shouldInitializeFields() {

        Customer customer =
                new Customer(
                        "C001",
                        "Ahmed",
                        "ahmed01",
                        "1234",
                        "ahmed@gmail.com"
                );

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