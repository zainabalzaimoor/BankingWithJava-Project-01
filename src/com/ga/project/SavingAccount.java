package com.ga.project;

public class SavingAccount extends Account{


    public SavingAccount(String account_number, DebitCard card) {
        super(account_number,"Saving",card);
    }
    public SavingAccount(String account_number,double balance) {
        super(account_number,"Saving",balance);
    }
}
