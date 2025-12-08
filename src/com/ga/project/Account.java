package com.ga.project;

import java.util.Date;

abstract class Account {

    //withdraw
    //deposit
    //overdraft
    //view balance
    private boolean isActive = true;
    private static int overdraftCount =0;
    private String account_number;
    private double balance;
    private String account_type;
    private DebitCard linkedCard;

    public Account(String account_number, String account_type,DebitCard linkedCard) {
        this.account_number = account_number;
        this.balance = 0.0;
        this.account_type = account_type;
        this.linkedCard = linkedCard;
    }
    public Account(String account_number, String account_type,double balance) {
        this.account_number = account_number;
        this.balance = balance;
        this.account_type = account_type;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public DebitCard getLinkedCard() {
        return linkedCard;
    }
    public String getLinkedCardName() {
        return linkedCard.getCardName();
    }

    public void withdraw(double amount){
        if (!isActive) {
            System.out.println("ALERT: Cannot withdraw. Account is currently deactivated.");
            return;
        }
        if(amount <= 0){
            System.out.println("Please enter valid number.");
            return;
        }
        if(balance <= 0) {
            if (amount > 100) {
                System.out.println("Transaction declined: Cannot withdraw more than $100 while the account balance is empty or negative.");
                return;
            }

            overdraftCount++;

            if (overdraftCount > 2) {
                isActive = false;
                System.out.println("ALERT: Account deactivated due to multiple overdrafts.");
                return;
            }
            else {
                double overdraft_fee = 35;
                balance -= overdraft_fee;
                System.out.println("Warning: Account is/will be overdrawn. A fee of $" + overdraft_fee + " will be applied.");
            }
        }

        balance -= amount;
        System.out.println("Withdrawing from the " + getAccount_type() + "Account: $" + amount + ".");
        System.out.println("Your new balance is: $" + balance);

    }
    public void deposit(double amount){
        if(amount < 0){
            System.out.println("Please enter valid number.");
            return;
        }
        double old_balance = balance;
        balance += amount;
        System.out.println("Depositing into the " + getAccount_type() + "Account: $" + amount);
        System.out.println("Your new balance is: $" + balance);

        if(!isActive && old_balance < 0 && balance >= 0){
            isActive = true;
            overdraftCount = 0;
            System.out.println("✅ SUCCESS: Account has been reactivated. Overdraft count has been reset.");
        }
    }

    public void transferMoney(Account from, Account to, double amount){
        if(amount < 0){
            System.out.println("Please enter valid number.");
            return;
        }
        if (from.equals(to)) {
            System.out.println("Transfer failed: Cannot transfer money to the same account.");
            return;
        }
        try {
            from.withdraw(amount);
            to.deposit(amount);
            System.out.println("SUCCESS: Transferred $" + amount +
                    " from " + from.getAccount_number() + " to " + to.getAccount_number() + ".");

        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Transfer failed due to: " + e.getMessage());
        }

    }


}
