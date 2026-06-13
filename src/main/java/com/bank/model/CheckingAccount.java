package com.bank.model;

import com.bank.exception.AccountClosedException;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;

public class CheckingAccount extends Account{
    public double overdraftLimit;

    public CheckingAccount (String accountNumber, double balance,double overdraftLimit){
        super(accountNumber, balance);
        this.overdraftLimit = overdraftLimit;
    }
    public double getOverdraftLimit(){
        return  overdraftLimit;
    }
    public void setOverdraftLimit(double overdraftLimit){
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount,Employee employee) throws BankException {
        lock.lock();
        try {
            if (amount <= 0) {
                throw new InvalidAmountException(amount);
            }
            if (closed) {
                throw new AccountClosedException(accountNumber);
            }
            if (balance - amount < -overdraftLimit) {
                throw new InsufficientFundsException(amount, balance + overdraftLimit);
            }
            balance -= amount;
        }finally {

            lock.unlock();

        }
    }
}
