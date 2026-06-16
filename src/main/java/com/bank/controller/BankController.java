package com.bank.controller;

import java.util.Collection;
import java.util.List;

import com.bank.thread.AlertMonitor;
import com.bank.thread.TransactionQueue;
import com.bank.thread.TransactionRequest;
import com.bank.thread.TransactionWorker;
import com.bank.model.TransactionType;

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

    private final TransactionQueue transactionQueue;
    private final TransactionWorker transactionWorker;
    private final Thread workerThread;

    private final AlertMonitor alertMonitor;
    private final Thread alertThread;

    public BankController() {
        this.bankService = new BankService();
        this.authService = new AuthService();

        this.transactionQueue = new TransactionQueue();
        this.transactionWorker = new TransactionWorker(transactionQueue, bankService);
        this.workerThread = new Thread(transactionWorker);
        this.workerThread.start();

        this.alertMonitor = new AlertMonitor(bankService, 100.0);
        this.alertThread = new Thread(alertMonitor);
        this.alertThread.start();
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

    public void requestDeposit(String accountNumber, double amount) {
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
        // this for maybe happend problem in and waiting or or sleeping.
        transactionWorker.stop();
        workerThread.interrupt();

        alertMonitor.stop();
        alertThread.interrupt();
    }


}