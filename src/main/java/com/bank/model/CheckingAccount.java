package com.bank.model;

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
    public void withdraw(double amount,Employee employee) {
        lock.lock();
        try {
            if (closed) {throw new RuntimeException( "Account is closed");
                }
            if (balance - amount < -overdraftLimit) {
                throw new RuntimeException(  "Insufficient funds");
            }
            balance -= amount;
        }finally {

            lock.unlock();

        }
    }
}