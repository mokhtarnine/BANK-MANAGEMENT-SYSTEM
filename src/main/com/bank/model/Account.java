package  com.bank.model;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Account{
    private String accountNumber;
    private double balance;
    private boolean closed;
    private  LinkedList<Transaction> transactions;
    private  ReentrantLock lock;
    public Account(String accountNumber, double balance,boolean closed){
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.closed = closed;
        transactions = new LinkedList<>();
        lock = new ReentrantLock();
    }
    public void deposit(double amount, Employee employee){
        balance += amount;
    }
    public void withdraw(double amout, Employee employee){
        balance -= amout;
    }
    public void closeAccount(){
        closed = true;
    }
    public void 


}