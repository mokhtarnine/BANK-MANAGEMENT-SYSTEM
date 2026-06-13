package com.bank.exception;

public class InvalidAmountException extends RuntimeException {
    private final double amount;
    public InvalidAmountException(double amount) {
        super("Invalid amount: "+ amount);
        this.amount = amount;
    }
    public double getAmount() {
        return amount;
    }
}