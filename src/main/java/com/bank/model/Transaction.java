package com.bank.model;

import java.time.LocalDateTime;



/**
 * Immutable record of one banking operation.
 *
 * A transaction is created after a successful deposit, withdrawal, or transfer.
 * Its fields are final, so the history cannot be accidentally changed after it
 * is added to an account.
 */
public final class Transaction{
    private final String transactionId ;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final double balanceAfter;
    private final Employee performedBy;

    public Transaction(String transactionId,TransactionType type,double  amount,LocalDateTime timestamp,double balanceAfter,Employee performedBy){
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.balanceAfter = balanceAfter;
        this.performedBy = performedBy;
    }
    public String getTransactionId(){
        return transactionId;
    }
    public TransactionType getType(){
        return type;
    }
    public double getAmount(){
        return amount;
    }
    public LocalDateTime getTimestamp(){
        return timestamp;
    }
    public double getBalanceAfter(){
        return balanceAfter;
    }

    public Employee getPerformedBy(){
        return performedBy;
    }


}
