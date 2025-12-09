package com.ga.project;

public interface DebitCard {
    double getWithdrawLimitPerDay();
    double getExternalTransferLimitPerDay();
    double getOwnAccountTransferLimitPerDay();
    double getOwnAccountDepositLimitPerDay();
    String getCardName();
}
