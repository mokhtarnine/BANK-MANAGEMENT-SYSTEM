package com.bank.service;

import com.bank.exception.BankException;
import com.bank.exception.AccountClosedException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.bank.repository.BankRepository;
import com.bank.repository.DatabaseManager;
import com.bank.repository.JdbcBankRepository;

public class BankService {
    /* role of this calss like brain of application Apply bank rules,Manage operations,Control models */

    private final ArrayList<Account> accounts;
    private final HashMap<String, Customer> customers;
    private final ReentrantLock transferLock;
    private final BankRepository repository;

    public BankService() {
        this(true);
    }

    public BankService(boolean persistenceEnabled) {
        this.accounts = new ArrayList<>();
        this.customers = new HashMap<>();
        this.transferLock = new ReentrantLock();
        this.repository = persistenceEnabled
                ? new JdbcBankRepository(new DatabaseManager())
                : null;

        loadDataSafely();
    }

    //  methdoe of crearteCustomer
    public Customer createCustomer(String id,String fullName,String username,String password,String email) {
        Customer customer = new Customer( id,fullName,username,password,email);
        customers.put(id, customer);
        saveDataSafely();
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

        saveDataSafely();

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
        account.addTransaction(createTransaction(
                TransactionType.DEPOSIT,
                amount,
                account,
                employee
        ));
        saveDataSafely();
    }

    public void withdraw(String accountNumber,double amount,Employee employee) throws BankException {
        Account account = findAccount(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.withdraw(amount, employee);
        account.addTransaction(createTransaction(
                TransactionType.WITHDRAW,
                amount,
                account,
                employee
        ));
        saveDataSafely();
    }
    /**
     * Transfers money between two accounts as one atomic operation.
     *
     * Both accounts remain locked during validation, balance changes, and
     * transaction-history creation. This prevents another thread from changing
     * either account in the middle of the transfer.
     */
    public void transfer(
            String fromAccountNumber,
            String toAccountNumber,
            double amount,
            Employee employee
    ) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException(
                    "Source and target accounts must be different."
            );
        }

        Account fromAccount = findAccount(fromAccountNumber);
        Account toAccount = findAccount(toAccountNumber);

        if (fromAccount == null) {
            throw new IllegalArgumentException("Source account not found: " + fromAccountNumber);
        }

        if (toAccount == null) {
            throw new IllegalArgumentException("Target account not found: " + toAccountNumber);
        }

        /*
         * transferLock allows only one transfer to acquire account locks at a
         * time. Deposits and withdrawals are still protected by each account's
         * own lock.
         */
        transferLock.lock();
        fromAccount.getLock().lock();
        toAccount.getLock().lock();

        try {
            /*
             * Validate the target before withdrawing from the source. Because
             * the target is locked, it cannot become closed after this check.
             */
            if (toAccount.isClosed()) {
                throw new AccountClosedException(
                        toAccount.getAccountNumber()
                );
            }

            fromAccount.withdraw(amount, employee);
            toAccount.deposit(amount, employee);

            fromAccount.addTransaction(createTransaction(
                    TransactionType.TRANSFER_OUT,
                    amount,
                    fromAccount,
                    employee
            ));
            toAccount.addTransaction(createTransaction(
                    TransactionType.TRANSFER_IN,
                    amount,
                    toAccount,
                    employee
            ));
            saveDataSafely();
        } finally {
            // Unlock in reverse order so every lock is released after success or failure.
            toAccount.getLock().unlock();
            fromAccount.getLock().unlock();
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
        saveDataSafely();
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

    private Transaction createTransaction(
            TransactionType type,
            double amount,
            Account account,
            Employee employee
    ) {
        return new Transaction(
                UUID.randomUUID().toString(),
                type,
                amount,
                LocalDateTime.now(),
                account.getBalance(),
                employee
        );
    }

    // load data / old data appears again
    private void loadDataSafely() {
        if (repository == null) {
            return;
        }

        try {
            repository.loadAll(customers, accounts);
        } catch (BankException e) {
            throw new IllegalStateException(
                    "Failed to load bank data",
                    e
            );
        }
    }

    //Try to save. If saving fails, throw runtime error with clear message.
    private void saveDataSafely() {
        if (repository == null) {
            return;
        }

        try {
            repository.saveAll(customers, accounts);
        } catch (BankException e) {
            throw new IllegalStateException(
                    "Failed to save bank data",
                    e
            );
        }
    }

}
