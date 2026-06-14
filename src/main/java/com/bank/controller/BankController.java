package com.bank.controller;

import java.util.Collection;
import java.util.List;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Employee;
import com.bank.model.Transaction;
import com.bank.service.AuthService;
import com.bank.service.BankService;

public class BankController {

    private final BankService bankService;
    private final AuthService authService;
    private Employee currentEmployee;

    public BankController() {
        this.bankService = new BankService();
        this.authService = new AuthService();
    }

    public boolean login(String username, String password) {
        Employee employee = authService.login(username, password);

        if (employee == null) {
            return false;
        }

        currentEmployee = employee;
        return true;
    }

    public Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public Customer createCustomer(
            String id,
            String fullName,
            String username,
            String password,
            String email
    ) {
        return bankService.createCustomer(
                id,
                fullName,
                username,
                password,
                email
        );
    }

    public Account openAccount(
            String customerId,
            String type,
            double initialBalance
    ) {
        return bankService.openAccount(
                customerId,
                type,
                initialBalance
        );
    }

    public void closeAccount(String accountNumber) throws BankException {
        bankService.closeAccount(accountNumber);
    }

    public void deposit(
            String accountNumber,
            double amount
    ) throws BankException {
        bankService.deposit(
                accountNumber,
                amount,
                currentEmployee
        );
    }

    public void withdraw(
            String accountNumber,
            double amount
    ) throws BankException {
        bankService.withdraw(
                accountNumber,
                amount,
                currentEmployee
        );
    }

    public void transfer(
            String fromAccountNumber,
            String toAccountNumber,
            double amount
    ) throws BankException {
        bankService.transfer(
                fromAccountNumber,
                toAccountNumber,
                amount,
                currentEmployee
        );
    }

    public Account findAccount(String accountNumber) {
        return bankService.findAccount(accountNumber);
    }

    public List<Customer> searchCustomers(String keyword) {
        return bankService.searchCustomers(keyword);
    }

    public List<Account> filterAccountsByType(String type) {
        return bankService.filterAccountsByType(type);
    }

    public Collection<Customer> getAllCustomers() {
        return bankService.getAllCustomers();
    }

    public List<Account> getAllAccounts() {
        return bankService.getAllAccounts();
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return bankService.getTransactionHistory(accountNumber);
    }
}