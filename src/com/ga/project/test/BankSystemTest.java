//package com.ga.project;
//
//import static org.junit.Assert.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class BankSystemTest {
//    private Customer mockCustomer;
//    private Account bankingAccount;
//    private MockFileService mockFileService;
//    private final double TITANIUM_WITHDRAW_LIMIT = 10000.00;
//    private final double TITANIUM_DEPOSIT_LIMIT = 200000.00;
//    private final double TITANIUM_EXT_TRANSFER_LIMIT = 20000.00;
//
//    // --- Mock Service Class to Control Usage History ---
//    private class MockFileService {
//        private final Map<String, Double> usageMap = new HashMap<>();
//
//        public void setTodayUsage(String type, double amount) {
//            usageMap.put(type.toUpperCase(), amount);
//        }
//
//        // Simulates the calculateTodayUsage method from the real FileService
//        public double calculateTodayUsage(String accountNumber, String transactionType, String filterType) {
//            return usageMap.getOrDefault(transactionType.toUpperCase(), 0.0);
//        }
//
//        // Mock persistence methods (needed for completeness but not fully tested here)
//        public void updateCustomerRecord(Customer c) {/* Mocked */}
//        public void appendTransaction(Customer c, String details) {/* Mocked */}
//    }
//
//    // Helper function to create the correct card instance
//    private DebitCard createCard(String type) {
//        return switch (type) {
//            case "Titanium" -> new MastercardTitanium();
//            default -> new MastercardStandard();
//        };
//    }
//
//    @BeforeEach
//    void setup() {
//        // Setup a customer with a Titanium card
//        DebitCard titaniumCard = createCard("Titanium");
//
//        // Account starts with $50,000.00 balance
//        bankingAccount = new BankingAccount("ACC-BA-TEST", 50000.00, titaniumCard);
//        List<Account> accounts = List.of(bankingAccount);
//
//        mockCustomer = new Customer("1001", "TestUser", "pass", "Customer", accounts);
//
//        // Initialize the mock service
//        mockFileService = new MockFileService();
//    }
//
//    // --- Core Limit Check Logic (Simulating BankApp Handlers) ---
//
//    // Simulates the daily limit check for a withdrawal (used in handleWithdraw)
//    private boolean checkWithdrawLimit(Account account, double amount) {
//        double currentUsage = mockFileService.calculateTodayUsage(account.getAccount_number(), "WITHDRAW", "today");
//        double limit = account.getLinkedCard().getWithdrawLimitPerDay();
//        return currentUsage + amount <= limit;
//    }
//
//    // Simulates the daily limit check for a deposit (used in handleDeposit)
//    private boolean checkDepositLimit(Account account, double amount) {
//        double currentUsage = mockFileService.calculateTodayUsage(account.getAccount_number(), "DEPOSIT", "today");
//        // Using the single deposit limit rule requested earlier
//        double limit = account.getLinkedCard().getOwnAccountDepositLimitPerDay();
//        return currentUsage + amount <= limit;
//    }
//
//    // Simulates the daily limit check for an external transfer (used in handleTransfer)
//    private boolean checkExternalTransferLimit(Account account, double amount) {
//        double currentUsage = mockFileService.calculateTodayUsage(account.getAccount_number(), "TRANSFER_OUT", "today");
//        double limit = account.getLinkedCard().getExternalTransferLimitPerDay();
//        return currentUsage + amount <= limit;
//    }
//
//
//    // =========================================================================
//    //                      7 LIMIT VALIDATION TEST CASES
//    // =========================================================================
//
//    /**
//     * Test 1: Daily Withdrawal Limit Exceeded
//     */
//    @Test
//    void test1_withdrawLimit_exceeded() {
//        // Titanium Withdraw Limit: $10,000.00
//        mockFileService.setTodayUsage("WITHDRAW", TITANIUM_WITHDRAW_LIMIT - 100.00); // $9,900 used
//        double excessiveAmount = 101.00; // Total usage: $10,001.00
//
//        assertFalse(checkWithdrawLimit(bankingAccount, excessiveAmount),
//                "Withdrawal must be blocked when total usage exceeds the limit.");
//    }
//
//    /**
//     * Test 2: Withdrawal Within Limit (Success Case)
//     */
//    @Test
//    void test2_withdrawLimit_withinBoundary() {
//        // Titanium Withdraw Limit: $10,000.00
//        mockFileService.setTodayUsage("WITHDRAW", TITANIUM_WITHDRAW_LIMIT / 2.0); // $5,000 used
//        double safeAmount = 5000.00;
//
//        assertTrue(checkWithdrawLimit(bankingAccount, safeAmount),
//                "Withdrawal must be allowed when the amount is exactly at the boundary limit.");
//    }
//
//    /**
//     * Test 3: Daily Deposit Limit Exceeded
//     */
//    @Test
//    void test3_depositLimit_exceeded() {
//        // Titanium Deposit Limit (Own Account Rule): $200,000.00
//        mockFileService.setTodayUsage("DEPOSIT", TITANIUM_DEPOSIT_LIMIT - 1000.00); // $199,000 used
//        double excessiveAmount = 1001.00; // Total usage: $200,001.00
//
//        assertFalse(checkDepositLimit(bankingAccount, excessiveAmount),
//                "Deposit must be blocked when total usage exceeds the limit.");
//    }
//
//    /**
//     * Test 4: Deposit With Zero Prior Usage (Success Case)
//     */
//    @Test
//    void test4_depositLimit_zeroUsage() {
//        // Titanium Deposit Limit: $200,000.00
//        mockFileService.setTodayUsage("DEPOSIT", 0.00);
//        double safeAmount = 150000.00;
//
//        assertTrue(checkDepositLimit(bankingAccount, safeAmount),
//                "Deposit must be allowed when daily usage is zero and amount is safe.");
//    }
//
//    /**
//     * Test 5: External Transfer Limit Exceeded
//     */
//    @Test
//    void test5_externalTransferLimit_exceeded() {
//        // Titanium External Transfer Limit: $20,000.00
//        mockFileService.setTodayUsage("TRANSFER_OUT", TITANIUM_EXT_TRANSFER_LIMIT - 500.00); // $19,500 used
//        double excessiveAmount = 501.00; // Total usage: $20,001.00
//
//        assertFalse(checkExternalTransferLimit(bankingAccount, excessiveAmount),
//                "External Transfer must be blocked when exceeding the lower external limit.");
//    }
//
//    /**
//     * Test 6: External Transfer Within Limit (Success Case)
//     */
//    @Test
//    void test6_externalTransferLimit_withinLimit() {
//        // Titanium External Transfer Limit: $20,000.00
//        mockFileService.setTodayUsage("TRANSFER_OUT", 1000.00); // $1,000 used
//        double safeAmount = 5000.00;
//
//        assertTrue(checkExternalTransferLimit(bankingAccount, safeAmount),
//                "External Transfer must be allowed when within the limit.");
//    }
//
//    /**
//     * Test 7: Withdrawal Fails on Insufficient Funds (Core Banking Check)
//     */
//    @Test
//    void test7_withdraw_insufficientFunds() {
//        // Set the account balance low (limit check will pass, but balance check should fail)
//        bankingAccount.setBalance(50.00);
//
//        // This test requires checking the actual result of the withdraw method,
//        // which typically throws an exception. We'll simulate that outcome here.
//
//        double withdrawalAmount = 100.00;
//
//        // Ensure the limit check passes (assuming 0 usage, limit is $10k)
//        assertTrue(checkWithdrawLimit(bankingAccount, withdrawalAmount), "Limit check should pass.");
//
//        // Verify the core operation throws InsufficientFundsException
//        assertThrows(InsufficientFundsException.class, () -> {
//            // Assuming your actual BankingAccount.withdraw() method throws this exception
//            bankingAccount.withdraw(withdrawalAmount);
//        }, "The core withdraw method must throw InsufficientFundsException.");
//    }
//}