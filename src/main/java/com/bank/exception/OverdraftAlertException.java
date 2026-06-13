package com.bank.exception;

public class OverdraftAlertException extends BankException {
    private final String accountNumber;
    private final double balance;
    public OverdraftAlertException(String accountNumber, double balance) {
        super( "Overdraft alert: account "+ accountNumber+" balance reached " + balance);
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