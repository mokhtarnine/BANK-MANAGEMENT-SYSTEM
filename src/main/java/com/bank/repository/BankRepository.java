package com.bank.repository;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Customer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Repository contract for persistence.
 *
 * BankService depends on this interface instead of a concrete database class.
 * That means the service only knows "save/load bank data", while
 * JdbcBankRepository decides how that data is stored in SQLite.
 */
public interface BankRepository {

    /**
     * Saves only customers.
     */
    void saveCustomers(HashMap<String, Customer> customers) throws BankException;

    /**
     * Loads customers from storage.
     */
    HashMap<String, Customer> loadCustomers() throws BankException;

    /**
     * Saves accounts and links every account to its owner customer.
     */
    void saveAccounts(HashMap<String, Customer> customers,ArrayList<Account> accounts) throws BankException;

    /**
     * Loads accounts and attaches them back to the matching customers.
     */
    ArrayList<Account> loadAccounts(HashMap<String, Customer> customers) throws BankException;

    /**
     * Saves customers, accounts, and transactions as one complete bank state.
     */
    void saveAll(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException;

    /**
     * Loads the full bank state into the collections given by BankService.
     */
    void loadAll(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException;
}
