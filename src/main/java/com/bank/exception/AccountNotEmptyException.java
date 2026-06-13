package com.bank.exception;

public class AccountNotEmptyException extends BankException {
    private final String accountNumber;
    private final double balance;

    public AccountNotEmptyException(String accountNumber, double balance) {
        super("Account " + accountNumber + " cannot be closed because balance is " + balance);
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }
}
