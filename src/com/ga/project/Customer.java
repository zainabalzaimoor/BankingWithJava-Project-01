package com.ga.project;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private List<Account> userAccounts = new ArrayList<>();

//    public Customer(String user_id, String user_name, String password, String role) {
//        super(user_id, user_name, password, role);
//    }

    public Customer(String user_id, String user_name, String password, String role, List<Account> userAccounts) {
        super(user_id, user_name, password, role);
        this.userAccounts = userAccounts;
    }

// has accounts and has transaction history
//    public Customer(String user_id, String user_name, String password, String role) {
//        super(user_id, user_name, password, role);
//    }

    public void addAccount(Account account){
        userAccounts.add(account);
    }
    public List<Account> getUserAccounts(){return userAccounts;}

    @Override
    public String toString() {
        return "Customer{" +
                "user_id='" + getUser_id() + '\'' +
                ", username='" + getUser_name() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", role='" + getRole() + '\'' +
                "userAccounts=" + userAccounts +
                '}';
    }
}
