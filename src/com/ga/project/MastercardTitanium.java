package com.ga.project;

public class MastercardTitanium implements DebitCard{
    @Override
    public double getWithdrawLimitPerDay() {
        return 10000;
    }

    @Override
    public double getExternalTransferLimitPerDay() {
        return 20000;
    }

    @Override
    public double getOwnAccountTransferLimitPerDay() {
        return 40000;
    }

    @Override
    public double getExternalDepositLimitPerDay() {
        return 100000;
    }

    @Override
    public double getOwnAccountDepositLimitPerDay() {
        return 200000;
    }

    @Override
    public String getCardName() {
        return "Mastercard Titanium";
    }
}
