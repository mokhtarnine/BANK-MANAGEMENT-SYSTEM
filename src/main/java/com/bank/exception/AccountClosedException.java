package com.bank.exception;

public class AccountClosedException extends BankException {
    // this exception for closed account
    private final String accountNumber;
    public AccountClosedException(String accountNumber) {
        super("Account "+ accountNumber + " is closed");
        this.accountNumber = accountNumber;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
}