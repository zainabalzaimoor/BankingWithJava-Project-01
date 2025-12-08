package com.ga.project;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
public class BankSystem {
    static AuthService authService = new AuthService();
    static FileService fileService = new FileService();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // password is Banker1234
        while (true) {
            System.out.println("--------------------------------------------------------");
            System.out.println("                Welcome to ACME Bank :) ");
            System.out.println("--------------------------------------------------------");

            System.out.println("Choose you're service: ");
            System.out.println("1. Banker Login");
            System.out.println("2. Customer Login");
            System.out.println("3. Activate your Account");
            System.out.println("4. Exit");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Enter banker username: ");
                    String adminUserName = scanner.nextLine();
                    System.out.print("Enter banker password: ");
                    String adminPassword = scanner.nextLine();

                    String adminLogin = authService.login(adminUserName, adminPassword);
                    if (adminLogin.equals("SUCCESS")) {
                        System.out.println("Banker logged in successfully!");
                        //redirect him to his own services
                        User loggedIn = authService.getLoggedInUser();
                        if (loggedIn instanceof Admin admin1) {
                            adminMenu(admin1);
                        }
                    }
                    break;
                case "2":
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    String loginStatus = authService.login(username, password);

                    if (loginStatus.equals("EMPTY_PASSWORD")) {
                        System.out.print("You need to set a password first!");
                        String newPassword = scanner.nextLine();
                        authService.setPassword(username, newPassword);
                        System.out.println("Password updated! Login again.");
                    } else if (loginStatus.equals("SUCCESS")) {
                        String role = authService.getLoggedInUser().getRole();
                        System.out.println(role + " " + username + " logged in successfully!");
                        //redirect him to his own services
                        User customer = authService.getLoggedInUser();
                        if (customer instanceof Customer customer1) {
                            customerMenu(customer1);
                        }

                    }
                    break;
                case "3":
                    System.out.print("Enter your username: ");
                    String name = scanner.nextLine();
                    System.out.print("please set a password:");
                    String pass = scanner.nextLine();
                    authService.setPassword(name, pass);
                    System.out.println("Your account has been activated!, you can Login now ｡◕‿◕｡ ");
                    break;

            }
        }

    }

    // --- Admin Menu ---
    public static void adminMenu(Admin admin) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Create Customer");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Customer ID: ");
                    String userId = scanner.nextLine();
                    System.out.print("Enter Customer username: ");
                    String username = scanner.nextLine();
                    admin.createCustomer(userId, username);
                    break;

                case "2":
                    authService.logout();
                    return;

                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // --- Customer Menu ---
    private static void customerMenu(Customer customer) {
        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("Welcome " + customer.getUser_name());
            System.out.println("1. View Accounts");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. View Detailed Account Statement \uD83E\uDDFE");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> viewAccounts(customer);
                case "2" -> handleDeposit(customer);
                case "3" -> handleWithdraw(customer);
                case "4" -> handleTransfer(customer);
                case "5" -> handleStatementFiltering(customer);
                case "6" -> {
                    authService.logout();
                    System.out.println("Logged out successfully!");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    // Helper method to display accounts (reused by other functions)
    private static void viewAccounts(Customer customer) {
        System.out.println("--- Accounts ---");
        if (customer.getUserAccounts().isEmpty()) {
            System.out.println("No accounts assigned yet.");
        } else {
            customer.getUserAccounts().forEach(acc ->
                    System.out.println(acc.getAccount_number() + " | Balance: " + acc.getBalance()));
        }
    }

    private static Account selectAccount(Customer customer, String prompt) {
        List<Account> accounts = customer.getUserAccounts();
        if (accounts.isEmpty()) {
            System.out.println("Operation failed: You have no accounts to select from.");
            return null;
        }

        System.out.println("\n--- Select an Account (" + prompt + ") ---");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            System.out.println((i + 1) + ". " + acc.getAccount_number() + " (Balance: " + acc.getBalance() + ")");
        }
        System.out.println("0. Cancel");
        System.out.print("Enter account number (1-" + accounts.size() + ") or 0 to cancel: ");

        try {
            int index = Integer.parseInt(scanner.nextLine());
            if (index == 0) {
                System.out.println("Operation cancelled.");
                return null;
            }
            if (index > 0 && index <= accounts.size()) {
                return accounts.get(index - 1);
            } else {
                System.out.println("Invalid selection.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }
    }
    private static void handleDeposit(Customer customer) {
        Account selectedAccount = selectAccount(customer, "Deposit");
        if (selectedAccount == null) {
            return;
        }

        System.out.print("Enter amount to deposit: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            // Call the deposit method on the selected account
            selectedAccount.deposit(amount);

            fileService.updateCustomerRecord(customer);
// Inside handleDeposit, replacing the details variable:
            String details = String.format(
                    "\nDATE: %tF %tT\nTYPE: DEPOSIT\nACCOUNT: %s\nAMOUNT: +%.2f\nNEW_BALANCE: %.2f\n#####",
                    new Date(), new Date(),
                    selectedAccount.getAccount_number(),
                    amount,
                    selectedAccount.getBalance()
            );
            fileService.appendTransaction(customer, details);

        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
        }
    }
    private static void handleWithdraw(Customer customer) {
        // Assuming 'scanner' and 'fileService' are accessible static fields
        // Assuming 'selectAccount' is a method that allows the user to choose an account

        Account selectedAccount = selectAccount(customer, "Withdraw");
        if (selectedAccount == null) {
            return;
        }

        DebitCard card = selectedAccount.getLinkedCard(); // Get the linked card for limits
        String accountNumber = selectedAccount.getAccount_number();

        System.out.print("Enter amount to withdraw: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Error: Amount must be positive.");
                return;
            }

            // --- NEW: DAILY LIMIT VALIDATION ---

            // 1. Calculate Today's Usage
            // Assume "WITHDRAW" is the type used in the log file for calculating usage
            double todayWithdrawals = FileService.calculateTodayUsage(customer, accountNumber, "WITHDRAW", "today");
            double dailyWithdrawLimit = card.getWithdrawLimitPerDay();

            // 2. Check Limit
            if (todayWithdrawals + amount > dailyWithdrawLimit) {
                System.out.printf("❌ Transaction Declined! Withdrawal exceeds daily limit of $%.2f set by your %s card.%n",
                        dailyWithdrawLimit, card.getCardName());
                System.out.printf("   > Usage Today: $%.2f / Limit: $%.2f%n", todayWithdrawals, dailyWithdrawLimit);
                return; // EXIT the method if the limit is exceeded
            }

            // --- END OF NEW VALIDATION ---

            // 4. Perform the withdrawal in memory
            double oldBalance = selectedAccount.getBalance(); // Capture balance before transaction
            selectedAccount.withdraw(amount); // This method should handle balance check/fees
            fileService.updateCustomerRecord(customer);

            // 5. Transaction Logging Preparation

            String transactionType = "WITHDRAW";
            double deductedAmount = amount;
            String notes = "N/A";

            // Logic to check for fee/overdraft:
            // If the resulting balance is less than the expected balance after withdrawal, a fee was charged.
            if (selectedAccount.getBalance() < (oldBalance - amount)) {
                // NOTE: This assumes the fee is always $35.0 as per your previous logic
                transactionType = "OVERDRAFT";
                notes = "Fee Applied: $35.0";
                // Deducted amount used for the log must include the fee
                deductedAmount = oldBalance - selectedAccount.getBalance();
            }

            // 6. Log the Transaction
            String details = String.format(
                    "\nDATE: %tF %tT\nTYPE: %s\nACCOUNT: %s\nAMOUNT: %.2f\nNEW_BALANCE: %.2f\nNOTES: %s\n#####",
                    new Date(), new Date(),
                    transactionType,
                    accountNumber,
                    -deductedAmount, // Ensure the amount is negative for the log
                    selectedAccount.getBalance(),
                    notes
            );

            fileService.appendTransaction(customer, details);
            System.out.printf("✅ Withdrawal of $%.2f successful.%n", amount);


        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount entered. Please enter a numerical value.");
        } catch (Exception e) {
            // Catch exceptions thrown by withdraw()
            System.out.println("❌ Withdrawal failed: " + e.getMessage());
        }
    }
    private static void handleTransfer(Customer customer) {
        // 1. Select the source account
        Account fromAccount = selectAccount(customer, "Transfer FROM");
        if (fromAccount == null) {
            return;
        }

        // Get the linked DebitCard for the source account
        DebitCard card = fromAccount.getLinkedCard();
        String sourceAccountNumber = fromAccount.getAccount_number();

        System.out.println("--- Destination Account ---");
        System.out.println("1. To one of my other accounts");
        System.out.println("2. To another customer's account");
        System.out.print("Choose destination type: ");
        String destinationChoice = scanner.nextLine();

        Account toAccount = null;
        Customer toCustomer = customer;
        boolean isOwnAccountTransfer = false; // Flag to track limit type

        if ("1".equals(destinationChoice)) {
            // Case 1: Transfer to own account
            toAccount = selectAccount(customer, "Transfer TO");
            if (toAccount == null) {
                return;
            }
            if (fromAccount.equals(toAccount)) {
                System.out.println("Transfer failed: Cannot transfer to the same source account.");
                return;
            }
            isOwnAccountTransfer = true; // Set flag for internal limit
        } else if ("2".equals(destinationChoice)) {
            // Case 2: Transfer to another customer's account (External)
            System.out.print("Enter the destination account number (e.g., ACC-BA-123): ");
            String destAccountNumber = scanner.nextLine().trim();

            toAccount = fileService.findAccountByNumber(destAccountNumber);

            if (toAccount == null) {
                System.out.println("Transfer failed: Destination account '" + destAccountNumber + "' not found.");
                return;
            }

            toCustomer = fileService.findCustomerByAccount(toAccount);
            if (toCustomer == null) {
                System.out.println("Transfer failed: Destination account owner could not be determined.");
                return;
            }
            // isOwnAccountTransfer remains false for external transfer
        } else {
            System.out.println("Invalid destination choice. Transfer cancelled.");
            return;
        }

        if (toAccount == null) {
            return;
        }

        System.out.print("Enter amount to transfer: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Error: Amount must be positive.");
                return;
            }

            // --- NEW: DAILY LIMIT VALIDATION ---

            String transactionType = "TRANSFER_OUT";
            double todayTransfers = FileService.calculateTodayUsage(customer, sourceAccountNumber, transactionType, "today");
            double dailyTransferLimit;
            String limitType;

            // 3. Select the correct daily limit based on transfer type
            if (isOwnAccountTransfer) {
                dailyTransferLimit = card.getOwnAccountTransferLimitPerDay();
                limitType = "Own Account Transfer Limit";
            } else {
                dailyTransferLimit = card.getExternalTransferLimitPerDay();
                limitType = "External Transfer Limit";
            }

            // 4. Check Limit
            if (todayTransfers + amount > dailyTransferLimit) {
                System.out.printf("❌ Transaction Declined! Transfer exceeds the %s of $%.2f set by your %s card.%n",
                        limitType, dailyTransferLimit, card.getCardName());
                System.out.printf("   > Usage Today: $%.2f / Limit: $%.2f%n", todayTransfers, dailyTransferLimit);
                return; // EXIT the method if the limit is exceeded
            }

            // --- END OF NEW VALIDATION ---


            // 5. Perform the atomic transfer
            // Note: The transferMoney method must handle the balance check (insufficient funds)
            fromAccount.transferMoney(fromAccount, toAccount, amount);

            // 6. Update master records for both customers
            fileService.updateCustomerRecord(customer);
            // Only update the recipient's file if they are a different customer
            if (!customer.getUser_id().equals(toCustomer.getUser_id())) {
                fileService.updateCustomerRecord(toCustomer);
            }

            // --- 7. TRANSACTION LOGGING ---

            // A. Log for the sender (TRANSFER_OUT)
            String fromDetails = String.format(
                    "\nDATE: %tF %tT\nTYPE: TRANSFER_OUT\nACCOUNT: %s\nAMOUNT: -%.2f\nNEW_BALANCE: %.2f\nTO_ACCOUNT: %s\n#####",
                    new Date(), new Date(),
                    sourceAccountNumber,
                    amount,
                    fromAccount.getBalance(),
                    toAccount.getAccount_number()
            );
            fileService.appendTransaction(customer, fromDetails);

            // B. Log for the receiver (TRANSFER_IN)
            String toDetails = String.format(
                    "\nDATE: %tF %tT\nTYPE: TRANSFER_IN\nACCOUNT: %s\nAMOUNT: +%.2f\nNEW_BALANCE: %.2f\nFROM_ACCOUNT: %s\n#####",
                    new Date(), new Date(),
                    toAccount.getAccount_number(),
                    amount,
                    toAccount.getBalance(),
                    sourceAccountNumber
            );
            fileService.appendTransaction(toCustomer, toDetails);

            System.out.println("✅ Transfer successful! 💸");

        } catch (NumberFormatException e) {
            System.out.println("❌ Transfer failed: Invalid amount entered.");
        } catch (Exception e) {
            // Catches exceptions thrown by the atomic transfer (e.g., insufficient funds)
            System.out.println("❌ Transfer failed: " + e.getMessage());
        }
    }

    // Inside your main application/handler class

// ... other imports ...

    public static void handleViewAccountStatement(Customer customer, String filterType) {

        // 1. Display Header (The total amount in the account)

        System.out.println("\n=======================================================");
        System.out.println("           🏦 DETAILED ACCOUNT STATEMENT 🏦           ");
        System.out.println("=======================================================");
        System.out.println("Customer: " + customer.getUser_name());
        System.out.println("Filter: " + filterType.toUpperCase());
        System.out.println("Report Date: " + String.format("%tF %tT", new Date(), new Date()));
        System.out.println("-------------------------------------------------------");

        // Display Current Balances (Total amount in the account)
        for (Account acc : customer.getUserAccounts()) {
            System.out.printf("Account: %s | Type: %s | CURRENT BALANCE: $%.2f%n",
                    acc.getAccount_number(),
                    acc instanceof BankingAccount ? "Banking Account" : "Saving Account",
                    acc.getBalance() // <--- Total amount in the account
            );
        }
        System.out.println("-------------------------------------------------------");


        // 2. CRITICAL STEP: Call the static FileService method to get the filtered data
        List<TransactionRecord> transactions = FileService.filterTransactions(customer, filterType);

        if (transactions.isEmpty()) {
            System.out.println("\nNO TRANSACTION HISTORY FOUND matching the filter (" + filterType + ").");
            System.out.println("=======================================================");
            return;
        }

        // 3. Display Transaction Table Header (with updated widths for alignment)
        System.out.println("\nTransaction History:");
        // Widths: Date(20), Type(12), Amount(8, right-aligned), Notes(35), New Balance(11, right-aligned)
        System.out.printf("| %-20s | %-12s | %8s | %-35s | %11s |%n",
                "Date & Time", "Type", "Amount", "Notes/Partner", "New Balance");
        // Separator line must match total width
        System.out.println("|----------------------|--------------|----------|-------------------------------------|-------------|");

        // 4. Display Transaction Rows
        for (TransactionRecord record : transactions) {
            // Calls the optimized toStatementRow() method from TransactionRecord
            System.out.println(record.toStatementRow());
        }

        System.out.println("=======================================================");
    }

    // You will need to ensure 'scanner' and 'fileService' are accessible (e.g., static fields).

    private static void handleStatementFiltering(Customer customer) {
        System.out.println("\n--- Select Transaction Filter ---");
        System.out.println("  [1] Today");
        System.out.println("  [2] Yesterday");
        System.out.println("  [3] Last 7 Days");
        System.out.println("  [4] Last 30 Days");
        System.out.println("  [5] All Time");
        System.out.print("Enter filter choice (1-5): ");

        String filterChoice = scanner.nextLine();

        String filterType = switch (filterChoice) {
            case "1" -> "today";
            case "2" -> "yesterday";
            case "3" -> "last 7 days";
            case "4" -> "last 30 days";
            case "5" -> "all";
            default -> "invalid";
        };

        if (!filterType.equals("invalid")) {
            // Call the detailed statement generation function, passing the filter type
            handleViewAccountStatement(customer,filterType);
        } else {
            System.out.println("Invalid filter choice. Showing All Time transactions.");
            handleViewAccountStatement(customer, "all"); // Default to 'all' on error
        }
    }

// NOTE: You must also ensure your handleViewAccountStatement method accepts the filterType parameter:
// public static void handleViewAccountStatement(Customer customer, String filterType) { ... }


//    private static void customerMenu(Customer customer) {
//        while (true) {
//            System.out.println("\n--- Customer Menu ---");
//            System.out.println("Welcome " + customer.getUser_name());
//            System.out.println("1. View Accounts");
//            System.out.println("2. Logout");
//            System.out.print("Choose an option: ");
//            String choice = scanner.nextLine();
//
//            switch (choice) {
//                case "1":
//                    System.out.println("--- Accounts ---");
//                    if (customer.getUserAccounts().isEmpty()) {
//                        System.out.println("No accounts assigned yet.");
//                    } else {
//                        customer.getUserAccounts().forEach(acc ->
//                                System.out.println(acc.getAccount_number() + " | Balance: " + acc.getBalance()));
//                    }
//                    break;
//
//                case "2":
//                    authService.logout();
//                    System.out.println("Logged out successfully!");
//                    return;
//
//                default:
//                    System.out.println("Invalid option!");
//            }
//        }
//    }
}
