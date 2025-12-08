package com.ga.project;

public class MastercardPlatinum implements DebitCard{


    @Override
    public double getWithdrawLimitPerDay() {
        return 20000;
    }

    @Override
    public double getExternalTransferLimitPerDay() {
        return 40000;
    }

    @Override
    public double getOwnAccountTransferLimitPerDay() {
        return 80000;
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
        return "Mastercard Platinum";
    }
}
