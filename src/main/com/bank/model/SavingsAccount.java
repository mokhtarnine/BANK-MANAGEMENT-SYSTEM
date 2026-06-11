package com.bank.model;

public class SavingsAccount extends  Account {
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
    public void applyInterest(){
        lock.lock();
        try {
            balance += balance * interestRate;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void withdraw(double amount,Employee employee) {
        lock.lock();
        try {
            if (closed) {throw new RuntimeException( "Account is closed");
                }
            if (amount > balance ) {
                throw new RuntimeException(  "Insufficient funds");
            }
            balance -= amount;
        }finally {

            lock.unlock();

        }
    }
}