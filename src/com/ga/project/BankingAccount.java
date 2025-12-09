package com.ga.project;

public class BankingAccount extends Account{

    public BankingAccount(String account_number,DebitCard card) {
        super(account_number, "Checking",card);
    }
    public BankingAccount(String account_number,double balance, DebitCard card) {
        super(account_number, "Checking",balance,card);
    }
}
