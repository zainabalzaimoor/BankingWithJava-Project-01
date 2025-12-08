package com.ga.project;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    //can add customers -- signup the users and assign them account by default (user_id based on user's input, acc_num auto generated)

    AuthService authService = new AuthService();

    public Admin(String user_id, String user_name, String password, String role) {
        super(user_id, user_name, password, role);
    }
    // Admin creates a customer
    public void createCustomer(String userId, String userName) {
        DebitCard bankingCard = new Mastercard();
        DebitCard savingCard = new Mastercard();

        String accountNumber1 = "ACC-" + "BA-"+ System.currentTimeMillis();
        BankingAccount account1 = new BankingAccount(accountNumber1,bankingCard);

        String accountNumber2 = "ACC-" + "SA-" + System.currentTimeMillis();
        SavingAccount account2 = new SavingAccount(accountNumber2,savingCard);


        List<Account> initialAccounts = new ArrayList<>();
        initialAccounts.add(account1);
        initialAccounts.add(account2);

        Customer customer = new Customer(userId,userName,"","Customer",initialAccounts);

        authService.signup(customer);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "user_id='" + getUser_id() + '\'' +
                ", username='" + getUser_name() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", role='" + getRole() + '\'' +
                "authService=" + authService +
                '}';
    }
}
