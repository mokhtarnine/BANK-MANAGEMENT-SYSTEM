package  com.bank.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public abstract class Account {
    protected  String accountNumber;
    protected double balance;
    protected boolean closed;
    protected List<Transaction> transactions;
    protected final ReentrantLock lock;

    public Account(String accountNumber, double balance){
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.closed = false;
        this.transactions = new LinkedList<>();
        this.lock = new ReentrantLock();
    }
    public String getAccountNumber(){
        return accountNumber;
    }
    public void deposit(double amount, Employee employee){
        lock.lock();
        try{
            balance += amount;
        } finally{
            lock.unlock();
        }
    }
    public abstract void withdraw(double amout, Employee employee);
    public void closeAccount(){
        if (balance == 0) {
            closed = true;
        }

    }
    public void addTransaction(Transaction transaction){
        transaction.add(transaction);
    }
    public List<Transaction> getTransaction(){
        return  transactions;
    }
    public double getBalance(){
        return balance;
    }
    public boolean  isClosed(){
        return closed;
    }
    public ReentrantLock getLock(){
        return lock;
    }
    

}