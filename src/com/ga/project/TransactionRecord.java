package com.ga.project;

import java.time.LocalDateTime;

public class TransactionRecord {
    public String dateTime;
    public LocalDateTime parsedDateTime;
    public String type;
    public String account;
    public double amount;
    public double newBalance;
    public String notes;

    @Override
    public String toString() {

        return String.format(
                "| %-20s | %-12s | %10.2f | %-20s | %11.2f |",
                dateTime,
                type,
                amount,
                notes,
                newBalance
        );
    }

    public String toStatementRow() {
        String signedAmount = String.format("%+9.2f", amount);

        return String.format(
                "| %-20s | %-12s | %s | %-35s | %11.2f |",
                dateTime,
                type,
                signedAmount,
                notes,
                newBalance
        );
    }
}