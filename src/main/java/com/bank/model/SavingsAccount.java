package com.bank.model;

import com.bank.exception.AccountClosedException;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;

public class SavingsAccount extends  Account {
    /*classe fo sabing type of accountss */
    private  double interestRate ;

    public SavingsAccount(String accountNumber, double balance,double interestRate){
        super(accountNumber, balance);
        this.interestRate = interestRate;
    }

    public double  getInterestRate(){
        return interestRate;
    }
    public void setInterestRate(double interestRate){
        this.interestRate = interestRate;
    }
    // methode of interest change about how interestRate of customer
    public void applyInterest(){
        lock.lock();
        try {
            balance += balance * interestRate;
        } finally {
            lock.unlock();
        }
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
            if (amount > balance ) {
                throw new InsufficientFundsException(amount, balance);
            }
            balance -= amount;
        }finally {

            lock.unlock();

        }
    }
}
