package com.bank.repository;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Customer;

import java.util.ArrayList;
import java.util.HashMap;

public interface BankRepository {
    /*this interface it say any repository must be able to save/load customers and accounts */

    void saveCustomers(HashMap<String, Customer> customers) throws BankException;

    HashMap<String, Customer> loadCustomers() throws BankException;

    void saveAccounts(HashMap<String, Customer> customers,ArrayList<Account> accounts) throws BankException;

    ArrayList<Account> loadAccounts(HashMap<String, Customer> customers) throws BankException;

    void saveAll(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException;

    void loadAll(
            HashMap<String, Customer> customers,
            ArrayList<Account> accounts
    ) throws BankException;
}