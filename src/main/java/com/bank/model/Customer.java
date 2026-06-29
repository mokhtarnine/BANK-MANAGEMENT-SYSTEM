package com.bank.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank customer.
 *
 * A customer extends User and owns a list of accounts. The repository rebuilds
 * this list when data is loaded from SQLite.
 */
public class Customer extends User {

    private String email;
    private  List<Account> accounts;

    public Customer(String id,String fullName,String username,String password,String email)
    {super(id,fullName,username,password); 
        this.email = email;
        this.accounts = new ArrayList<>();
    }
    public  String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public List<Account> getAccounts(){
        return accounts;
    }

    /**
     * Links an account to this customer.
     */
    public void addAccount(Account account){
        accounts.add(account);
    }

    /**
     * Removes the link between this customer and the given account.
     */
    public void removeAccount(Account account){
        accounts.remove(account);
    }
    
    @Override
    public String getRole(){
        return "CUSTOMER";
    }

}
