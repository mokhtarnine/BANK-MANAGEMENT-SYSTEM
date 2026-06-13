package com.bank.model;

public class Employee extends User{
    private String role;

    public Employee (String id,String fullName,String username,String password,String role){
        super(id,fullName,username,password);
        this.role = role;
    }
    public String getRole(){
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
