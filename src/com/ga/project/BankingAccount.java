package com.ga.project;

public class BankingAccount extends Account{

    public BankingAccount(String account_number,DebitCard card) {
        super(account_number, "Banking",card);
    }
    public BankingAccount(String account_number,double balance) {
        super(account_number, "Banking",balance);
    }
}
