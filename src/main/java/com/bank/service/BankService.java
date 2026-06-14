package com.bank.service;

import com.bank.exception.BankException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

public class BankService {
    /* role of this calss like brain of application Apply bank rules,Manage operations,Control models */

    private final ArrayList<Account> accounts;
    private final HashMap<String, Customer> customers;
    private final ReentrantLock transferLock;

    public BankService() {
        this.accounts = new ArrayList<>();
        this.customers = new HashMap<>();
        this.transferLock = new ReentrantLock();
    }

    //  methdoe of crearteCustomer
    public Customer createCustomer(String id,String fullName,String username,String password,String email) {
        Customer customer = new Customer( id,fullName,username,password,email);
        customers.put(id, customer);
        return customer;
    }
    // get all customers
    public Collection<Customer> getAllCustomers(){
        return customers.values();
    }
    // open Account
    public Account openAccount(String customerId,String type,double initialBalance) {
        Customer customer = customers.get(customerId);

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        if (initialBalance < 0) {
            throw new InvalidAmountException(initialBalance);
        }

        String accountNumber = "ACC" + (accounts.size() + 1);

        Account account;

        if (type.equalsIgnoreCase("CHECKING")) {
            account = new CheckingAccount(accountNumber,initialBalance,500.0);
        } else if (type.equalsIgnoreCase("SAVINGS")) {
            account = new SavingsAccount(accountNumber,initialBalance,0.05);
        } else {
            throw new IllegalArgumentException("Invalid account type: " + type);
        }

        accounts.add(account);
        customer.addAccount(account);

        return account;
    }
    // find account 
    public Account findAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }

        return null;
    }
    // Add Deposit and Withdraw
    public void deposit(String accountNumber,double amount,Employee employee) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        account.deposit(amount, employee);
    }

    public void withdraw(String accountNumber,double amount,Employee employee) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.withdraw(amount, employee);
    }
    // Add Transfer
    public void transfer( String fromAccountNumber,String toAccountNumber,double amount,Employee employee) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        Account fromAccount = findAccount(fromAccountNumber);
        Account toAccount = findAccount(toAccountNumber);

        if (fromAccount == null) {
            throw new IllegalArgumentException("Source account not found: " + fromAccountNumber);
        }

        if (toAccount == null) {
            throw new IllegalArgumentException("Target account not found: " + toAccountNumber);
        }

        transferLock.lock();

        try {
            fromAccount.withdraw(amount, employee);
            toAccount.deposit(amount, employee);
        } finally {
            transferLock.unlock();
        }
    }
    // Add Search Methods
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> result = new ArrayList<>();
            //If customer name contains keyword
            //OR customer id contains keyword
            //OR customer email contains keyword
            //then add this customer to the search result.

        for (Customer customer : customers.values()) {
            if (customer.getFullName().toLowerCase().contains(keyword.toLowerCase())
                    || customer.getId().toLowerCase().contains(keyword.toLowerCase())
                    || customer.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(customer);
            }
        }

        return result;
    }
    // get all accounts
    public List<Account> getAllAccounts() {
        return accounts;
    }
    // close Account 
    public void closeAccount(String accountNumber) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        account.closeAccount();
    }
    // filter Account by type 
    public List<Account> filterAccountsByType(String type) {
        List<Account> result = new ArrayList<>();

        for (Account account : accounts) {
            if (type.equalsIgnoreCase("CHECKING") && account instanceof CheckingAccount) {
                result.add(account);
            } else if (type.equalsIgnoreCase("SAVINGS") && account instanceof SavingsAccount) {
                result.add(account);
            }
        }

        return result;
    }
    // get transaction history
    public List<Transaction> getTransactionHistory(String accountNumber) {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        return account.getTransaction();
    }




}
