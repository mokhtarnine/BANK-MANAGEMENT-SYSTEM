package com.bank.model;

import com.bank.model.User;
import java.util.ArrayList;
import java.util.List;

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
    public void addAccount(Account account){
        accounts.add(account);
    }
    public void removeAccount(Accounts account){
        accounts.remove(account);
    }
    
    @Override
    public String getRole(){
        return "CUSTOMER";
    }

}