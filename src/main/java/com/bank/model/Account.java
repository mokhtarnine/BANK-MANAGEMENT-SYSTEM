package  com.bank.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.bank.exception.AccountClosedException;
import com.bank.exception.AccountNotEmptyException;
import com.bank.exception.BankException;
import com.bank.exception.InvalidAmountException;


/**
 * Base class for all account types.
 *
 * Common behavior, such as deposit, closing an account, transaction history,
 * and thread locking, is kept here. Different account types implement their own
 * withdraw rules by overriding withdraw().
 */
public abstract class Account {
    protected  String accountNumber;
    protected double balance;
    protected boolean closed;
    protected List<Transaction> transactions;
    protected final ReentrantLock lock; // Protects balance changes between threads.

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

    /**
     * Adds money to the account after checking that the amount is positive and
     * the account is not closed.
     */
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
    /**
     * Withdraw is abstract because checking and savings accounts have different
     * rules. Checking can use overdraft, while savings cannot go below zero.
     */
    public abstract void withdraw(double amount, Employee employee) throws BankException;

    /**
     * Closes the account only when it is open and has zero balance.
     */
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
    /**
     * Used by the repository when loading saved account rows from SQLite.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    

}
