package com.bank.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Employee;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import com.bank.service.AuditService;
import com.bank.service.AuthService;
import com.bank.service.BankService;
import com.bank.thread.AlertMonitor;
import com.bank.thread.TransactionQueue;
import com.bank.thread.TransactionRequest;
import com.bank.thread.TransactionWorker;

/**
 * Central controller of the Bank Management System.
 *
 * This class acts as the communication layer between the graphical
 * user interface and the service layer.
 *
 * Responsibilities:
 * - manage employee authentication
 * - create and manage customers
 * - create and manage accounts
 * - execute banking operations
 * - coordinate transaction processing
 * - record audit actions and warnings
 * - manage background worker threads
 *
 * The controller delegates business rules to BankService and avoids
 * direct interaction between the GUI and persistence layer.
 */
public class BankController {
            
    private final BankService bankService;
    private final AuthService authService;
    private final AuditService auditService;
    private Employee currentEmployee;

    private final TransactionQueue transactionQueue;
    private final TransactionWorker transactionWorker;
    private final Thread workerThread;

    private final AlertMonitor alertMonitor;
    private final Thread alertThread;

    public BankController() {
        this(true);
    }

    /**
     * Creates the controller and starts the background worker threads.
     *
     * @param persistenceEnabled true for the real application, false for unit
     *        tests that should not touch the real SQLite database
     */
    public BankController(boolean persistenceEnabled) {
        this.bankService = new BankService(persistenceEnabled);
        this.authService = new AuthService();
        this.auditService = new AuditService();

        this.transactionQueue = new TransactionQueue();
        this.transactionWorker = new TransactionWorker(
                transactionQueue,
                bankService,
                auditService
        );
        this.workerThread = new Thread(transactionWorker);
        this.workerThread.start();

        this.alertMonitor = new AlertMonitor(
                bankService,
                auditService,
                100.0
        );
        this.alertThread = new Thread(alertMonitor);
        this.alertThread.start();
    }

    public boolean login(String username, String password) {
        Employee employee = authService.login(username, password);

        if (employee == null) {
            auditService.recordWarning(
                    "Rejected login attempt for username: " + username
            );
            return false;
        }

        currentEmployee = employee;
        auditService.recordAction(
                currentEmployee,
                "Logged in successfully"
        );
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
        try {
            Customer customer = bankService.createCustomer(
                    id,
                    fullName,
                    username,
                    password,
                    email
            );

            auditService.recordAction(
                    currentEmployee,
                    "Created customer " + id
            );

            return customer;
        } catch (RuntimeException e) {
            auditService.recordError("Could not create customer " + id, e);
            throw e;
        }
    }

    public Account openAccount(
            String customerId,
            String type,
            double initialBalance
    ) {
        try {
            Account account = bankService.openAccount(
                    customerId,
                    type,
                    initialBalance
            );

            auditService.recordAction(
                    currentEmployee,
                    "Opened " + type + " account "
                            + account.getAccountNumber()
            );

            return account;
        } catch (RuntimeException e) {
            auditService.recordError(
                    "Could not open account for customer " + customerId,
                    e
            );
            throw e;
        }
    }

    public void closeAccount(String accountNumber) throws BankException {
        try {
            bankService.closeAccount(accountNumber);
            auditService.recordAction(
                    currentEmployee,
                    "Closed account " + accountNumber
            );
        } catch (BankException e) {
            auditService.recordWarning(
                    "Could not close account " + accountNumber
                            + ": " + e.getMessage()
            );
            throw e;
        } catch (RuntimeException e) {
            auditService.recordError(
                    "Could not close account " + accountNumber,
                    e
            );
            throw e;
        }
    }

    public void deposit(
            String accountNumber,
            double amount
    ) throws BankException {
        try {
            bankService.deposit(accountNumber, amount, currentEmployee);
            auditService.recordAction(
                    currentEmployee,
                    "Deposited " + amount + " into account " + accountNumber
            );
        } catch (BankException e) {
            auditService.recordWarning(
                    "Deposit rejected for account " + accountNumber
                            + ": " + e.getMessage()
            );
            throw e;
        } catch (RuntimeException e) {
            auditService.recordError(
                    "Deposit failed for account " + accountNumber,
                    e
            );
            throw e;
        }
    }

    public void withdraw(
            String accountNumber,
            double amount
    ) throws BankException {
        try {
            bankService.withdraw(accountNumber, amount, currentEmployee);
            auditService.recordAction(
                    currentEmployee,
                    "Withdrew " + amount + " from account " + accountNumber
            );
        } catch (BankException e) {
            auditService.recordWarning(
                    "Withdrawal rejected for account " + accountNumber
                            + ": " + e.getMessage()
            );
            throw e;
        } catch (RuntimeException e) {
            auditService.recordError(
                    "Withdrawal failed for account " + accountNumber,
                    e
            );
            throw e;
        }
    }

    public void transfer(
            String fromAccountNumber,
            String toAccountNumber,
            double amount
    ) throws BankException {
        try {
            bankService.transfer(
                    fromAccountNumber,
                    toAccountNumber,
                    amount,
                    currentEmployee
            );

            auditService.recordAction(
                    currentEmployee,
                    "Transferred " + amount
                            + " from " + fromAccountNumber
                            + " to " + toAccountNumber
            );
        } catch (BankException e) {
            auditService.recordWarning(
                    "Transfer rejected from " + fromAccountNumber
                            + " to " + toAccountNumber
                            + ": " + e.getMessage()
            );
            throw e;
        } catch (RuntimeException e) {
            auditService.recordError(
                    "Transfer failed from " + fromAccountNumber
                            + " to " + toAccountNumber,
                    e
            );
            throw e;
        }
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

    public void requestDeposit(String accountNumber, double amount) {
        /*
         * Queue-based methods are used when a transaction should be processed by
         * the background TransactionWorker instead of immediately on the GUI
         * thread.
         */
        TransactionRequest request = new TransactionRequest(
                TransactionType.DEPOSIT,
                accountNumber,
                null,
                amount,
                currentEmployee
        );

        transactionQueue.enqueue(request);
    }

    public void requestWithdraw(String accountNumber, double amount) {
        TransactionRequest request = new TransactionRequest(
                TransactionType.WITHDRAW,
                accountNumber,
                null,
                amount,
                currentEmployee
        );

        transactionQueue.enqueue(request);
    }
    public void requestTransfer(
            String fromAccountNumber,
            String toAccountNumber,
            double amount
    ) {
        TransactionRequest request = new TransactionRequest(
                TransactionType.TRANSFER_OUT,
                fromAccountNumber,
                toAccountNumber,
                amount,
                currentEmployee
        );

        transactionQueue.enqueue(request);
    }

    public void shutdownThreads() {
        /*
         * Stops background threads before the application closes. This prevents
         * worker threads from staying alive after the Swing window is closed.
         */
        transactionWorker.stop();
        workerThread.interrupt();

        alertMonitor.stop();
        alertThread.interrupt();
    }

    public int getTotalCustomers() {
    return bankService.getAllCustomers().size();
    }

    public int getTotalAccounts() {
        return bankService.getAllAccounts().size();
    }

    public double getTotalBalance() {
        double total = 0;

        for (Account account : bankService.getAllAccounts()) {
            total += account.getBalance();
        }

        return total;
    }

    public Object[][] getCustomerAccountTableData(String customerId) {
        return getCustomerAccountTableData(customerId, "", "ALL");
    }

    /**
     * Builds account-table rows for one customer using optional filters.
     *
     * The GUI receives simple table data and does not need to inspect Account
     * objects or call BankService directly.
     *
     * @param customerId customer whose accounts should be displayed
     * @param accountNumberKeyword full or partial account number
     * @param accountType ALL, CHECKING, or SAVINGS
     * @return rows containing account number, type, balance, and closed status
     */
    public Object[][] getCustomerAccountTableData(
            String customerId,
            String accountNumberKeyword,
            String accountType
    ) {
        Customer selectedCustomer = null;

        for (Customer customer : bankService.getAllCustomers()) {
            if (customer.getId().equals(customerId)) {
                selectedCustomer = customer;
                break;
            }
        }

        if (selectedCustomer == null) {
            return new Object[0][0];
        }

        String normalizedKeyword = accountNumberKeyword == null
                ? ""
                : accountNumberKeyword.trim().toLowerCase();

        String normalizedType = accountType == null
                ? "ALL"
                : accountType.trim().toUpperCase();

        List<Account> matchingAccounts = new ArrayList<>();

        for (Account account : selectedCustomer.getAccounts()) {
            String typeName = getAccountTypeName(account);

            boolean numberMatches = account.getAccountNumber()
                    .toLowerCase()
                    .contains(normalizedKeyword);

            boolean typeMatches = normalizedType.equals("ALL")
                    || typeName.equals(normalizedType);

            if (numberMatches && typeMatches) {
                matchingAccounts.add(account);
            }
        }

        Object[][] data = new Object[matchingAccounts.size()][4];

        for (int i = 0; i < matchingAccounts.size(); i++) {
            Account account = matchingAccounts.get(i);

            data[i][0] = account.getAccountNumber();
            data[i][1] = getAccountTypeName(account);
            data[i][2] = account.getBalance();
            data[i][3] = account.isClosed();
        }

        return data;
    }

    private String getAccountTypeName(Account account) {
        String className = account.getClass().getSimpleName();

        if (className.equals("CheckingAccount")) {
            return "CHECKING";
        }

        if (className.equals("SavingsAccount")) {
            return "SAVINGS";
        }

        return className;
    }

    /**
     * Lets the GUI register a function that will receive alert messages from
     * AlertMonitor. MainFrame uses this to update AlertLogPanel safely.
     */
    public void setAlertHandler(java.util.function.Consumer<String> alertHandler) {
        alertMonitor.setAlertHandler(alertHandler);
    }




}
