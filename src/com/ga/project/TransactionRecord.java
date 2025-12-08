package com.ga.project;

// Create a new class file named TransactionRecord.java
// Create a new class file named TransactionRecord.java (or update your existing one)

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
        // Alignment Explanation:
        // 1. Column Headers (e.g., Date & Time, Type): Use '-' flag for left-justification.
        // 2. Amount and New Balance: Omit the '-' flag for right-justification.
        // 3. Amount: Use the '+' flag (as in %+f) to force the display of the sign (+ or -).

        return String.format(
                "| %-20s | %-12s | %10.2f | %-20s | %11.2f |",
                dateTime,
                type,
                amount, // RIGHT-JUSTIFIED, SIGNED (using %+f in print statement)
                notes,
                newBalance // RIGHT-JUSTIFIED
        );
    }

    // NOTE: The previous recommended change (using the '+' flag) must be implemented
    // where the formatting is *applied* (e.g., in the handler method or where you call toString),
    // or you can simplify the toString() if you change the format of 'amount' to always include the sign.

    // For the specific right-alignment you want, we need a slight adjustment,
    // as the amount sign is critical for the right-justified column.
    // The safest way is to format the signed amount separately:

    public String toStatementRow() {
        // Format the amount with the sign and width (e.g., +70.00 will use 7 chars)
        String signedAmount = String.format("%+8.2f", amount);

        // Now use the pre-formatted string in the main layout
        return String.format(
                "| %-20s | %-12s | %s | %-20s | %11.2f |",
                dateTime,
                type,
                signedAmount, // Used the pre-formatted string here
                notes,
                newBalance
        );
    }
}