package com.bank.thread;

import com.bank.model.Employee;
import com.bank.model.TransactionType;

public class TransactionRequest {
    // Represents a transaction request waiting to be processed by a worker thread.

    private final TransactionType type;
    private final String fromAccount;
    private final String toAccount;
    private final double amount;
    private final Employee employee;

    public TransactionRequest(
            TransactionType type,
            String fromAccount,
            String toAccount,
            double amount,
            Employee employee
    ) {
        this.type = type;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.employee = employee;
    }

    public TransactionType getType() {
        return type;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public double getAmount() {
        return amount;
    }

    public Employee getEmployee() {
        return employee;
    }
}