package  com.bank.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.bank.exception.AccountClosedException;
import com.bank.exception.AccountNotEmptyException;
import com.bank.exception.BankException;
import com.bank.exception.InvalidAmountException;


public abstract class Account {
    /*this abstract classe for many type of account and transaction */
    protected  String accountNumber;
    protected double balance;
    protected boolean closed;
    protected List<Transaction> transactions;
    protected final ReentrantLock lock; // for multiple threads at same time

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
    public void deposit(double amount, Employee employee) throws AccountClosedException {
        lock.lock();
        try{
            if (amount <= 0) {
                throw new InvalidAmountException(amount);
            }
            if (closed) {
                throw new AccountClosedException(accountNumber);
            }
            balance += amount;
        } finally{
            lock.unlock();
        }
    }
    // here we don't implement of withdraw methode because it change behaver with diffrent classe
    public abstract void withdraw(double amount, Employee employee) throws BankException;
    public void closeAccount() throws BankException {
        lock.lock();
        try {
            if (closed) {
                throw new AccountClosedException(accountNumber);
            }
            if (balance != 0) {
                throw new AccountNotEmptyException(accountNumber, balance);
            }
            closed = true;
        } finally {
            lock.unlock();
        }
    }
    public void addTransaction(Transaction transaction){
        transactions.add(transaction);
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
    // this methode for repositry can set colse by this 
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    

}
