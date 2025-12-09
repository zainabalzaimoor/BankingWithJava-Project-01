package com.ga.project;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
public class BankSystem {

    static AuthService authService = new AuthService();
    static FileService fileService = new FileService();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
        System.out.println("\n========================================================");
        System.out.println("              ✨ Welcome to ACME Bank! ✨              ");
        System.out.println("========================================================");
            System.out.println("Choose your service:");
            System.out.println("1. 👨‍💼 Banker Login");
            System.out.println("2. 👤 Customer Login");
            System.out.println("3. 🔑 Activate Your Account");
            System.out.println("4. 🚪 Exit");
            System.out.println("--------------------------------------------------------");
            System.out.print("Option: ");
            String option = scanner.nextLine().trim();

            switch (option) {
                case "1" -> {
                    System.out.print("Enter banker username: ");
                    String adminUserName = scanner.nextLine();
                    System.out.print("Enter banker password: ");
                    String adminPassword = scanner.nextLine();

                    String status = authService.login(adminUserName, adminPassword);

                    if (status.equals("SUCCESS")) {
                        System.out.println("✅ Banker logged in successfully!");
                        User loggedIn = authService.getLoggedInUser();
                        if (loggedIn instanceof Admin admin1) {
                            adminMenu(admin1); // Redirect to admin services
                        }
                    } else {
                        System.out.println("❌ Login failed: " + status);
                    }
                }

                case "2" -> {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();

                    if (!authService.checkUsers(username)) {
                        System.out.println("⚠️ Username does not exist!");
                        break;
                    }

                    if (authService.userHasNoPassword(username)) {
                        System.out.println("⚠️ Account not activated. Please use option 3 to set your password first.");
                        break;
                    }

                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    String status = authService.login(username, password);

                    switch (status) {
                        case "SUCCESS" -> {
                            User customer = authService.getLoggedInUser();
                            System.out.println("✅ Welcome, " + customer.getUser_name() + "!");
                            if (customer instanceof Customer c) {
                                customerMenu(c);
                            }
                        }
                        case "LOCKED_ACCOUNT" ->
                                System.out.println("🔒 Account is locked due to too many failed attempts. Please wait one minute.");
                        case "WRONG_PASSWORD" ->
                                System.out.println("❌ Incorrect password. You have " + authService.getAttemptsLeft() + " attempts left!");
                        default ->
                                System.out.println("❌ Login failed.");
                    }
                }

                case "3:" -> {
                        System.out.println("\n--- 🔑 Account Activation ---");
                        System.out.print("Enter your username: ");
                        String name = scanner.nextLine();

                        if (authService.userHasNoPassword(name)) {
                            System.out.print("Please set a secure password: ");
                            String pass = scanner.nextLine();
                            authService.setPassword(name, pass);
                            System.out.println("🎉 Your account has been successfully activated! You can Login now ｡◕‿◕｡ .");

                        } else {
                            System.out.println("❌ Error: Account not found or already has a password set.");
                        }
                }
                case "4" -> {
                        System.out.println("👋 Thank you for banking with us. Goodbye!");
                        System.exit(0);
                }
                default -> System.out.println("🚫 Invalid option. Please enter a number from the menu.");
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
                case "1" -> {
                    System.out.print("Enter Customer ID: ");
                    String userId = scanner.nextLine();
                    System.out.print("Enter Customer username: ");
                    String username = scanner.nextLine();
                    admin.createCustomer(userId, username);
                }
                case "2" -> {
                    authService.logout();
                    return;
                }
                default -> System.out.println("Invalid option!");
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
            System.out.println("5. View Detailed Account Statement 🏦");
            System.out.println("6. Filter Transactions 🧾");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> viewAccounts(customer);
                case "2" -> handleDeposit(customer);
                case "3" -> handleWithdraw(customer);
                case "4" -> handleTransfer(customer);
                case "5" -> handleViewAccountStatement(customer);
                case "6" -> handleStatementFiltering(customer);
                case "7" -> {
                    authService.logout();
                    System.out.println("Logged out successfully!");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }



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

        DebitCard card = selectedAccount.getLinkedCard();
        String accountNumber = selectedAccount.getAccount_number();
        String transactionType = "DEPOSIT";

        System.out.print("Enter amount to deposit: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Error: Amount must be positive.");
                return;
            }

            double todayDeposits = fileService.calculateTodayUsage(customer, accountNumber, transactionType);

            double dailyDepositLimit = card.getOwnAccountDepositLimitPerDay();
            String limitType = "Daily Deposit Limit";

            if (todayDeposits + amount > dailyDepositLimit) {
                System.out.printf("❌ Transaction Declined! Deposit exceeds the %s of $%.2f set by your %s card.%n",
                        limitType, dailyDepositLimit, card.getCardName());
                System.out.printf("   > Usage Today: $%.2f / Limit: $%.2f%n", todayDeposits, dailyDepositLimit);
                return;
            }

            selectedAccount.deposit(amount);

            fileService.updateCustomerRecord(customer);

            String details = String.format(
                    "\nDATE: %tF %tT\nTYPE: %s\nACCOUNT: %s\nAMOUNT: +%.2f\nNEW_BALANCE: %.2f\nNOTES: %s\n#####",
                    new Date(), new Date(),
                    transactionType,
                    accountNumber,
                    amount,
                    selectedAccount.getBalance(),
                    "Deposit processed"
            );
            fileService.appendTransaction(customer, details);
            System.out.printf("✅ Deposit of $%.2f into %s successful.%n", amount, accountNumber);

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount entered.");
        } catch (Exception e) {
            System.out.println("❌ Deposit failed: " + e.getMessage());
        }
    }
    private static void handleWithdraw(Customer customer) {
        Account selectedAccount = selectAccount(customer, "Withdraw");
        if (selectedAccount == null) {
            return;
        }

        DebitCard card = selectedAccount.getLinkedCard();
        String accountNumber = selectedAccount.getAccount_number();

        System.out.print("Enter amount to withdraw: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Error: Amount must be positive.");
                return;
            }

            double todayWithdrawals = fileService.calculateTodayUsage(customer, accountNumber, "WITHDRAW");
            double dailyWithdrawLimit = card.getWithdrawLimitPerDay();

            if (todayWithdrawals + amount > dailyWithdrawLimit) {
                System.out.printf("❌ Transaction Declined! Withdrawal exceeds daily limit of $%.2f set by your %s card.%n",
                        dailyWithdrawLimit, card.getCardName());
                System.out.printf("   > Usage Today: $%.2f / Limit: $%.2f%n", todayWithdrawals, dailyWithdrawLimit);
                return;
            }

            double oldBalance = selectedAccount.getBalance();
            selectedAccount.withdraw(amount);
            fileService.updateCustomerRecord(customer);

            String transactionType = "WITHDRAW";
            double deductedAmount = amount;
            String notes = "N/A";

            if (selectedAccount.getBalance() < (oldBalance - amount)) {

                transactionType = "OVERDRAFT";
                notes = "Fee Applied: $35.0";

                deductedAmount = oldBalance - selectedAccount.getBalance();
            }

            String details = String.format(
                    "\nDATE: %tF %tT\nTYPE: %s\nACCOUNT: %s\nAMOUNT: %.2f\nNEW_BALANCE: %.2f\nNOTES: %s\n#####",
                    new Date(), new Date(),
                    transactionType,
                    accountNumber,
                    -deductedAmount,
                    selectedAccount.getBalance(),
                    notes
            );

            fileService.appendTransaction(customer, details);
            System.out.printf("✅ Withdrawal of $%.2f successful.%n", amount);


        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount entered. Please enter a numerical value.");
        } catch (Exception e) {
            System.out.println("❌ Withdrawal failed: " + e.getMessage());
        }
    }
    private static void handleTransfer(Customer customer) {
        Account fromAccount = selectAccount(customer, "Transfer FROM");
        if (fromAccount == null) {
            return;
        }

        DebitCard card = fromAccount.getLinkedCard();
        String sourceAccountNumber = fromAccount.getAccount_number();

        System.out.println("--- Destination Account ---");
        System.out.println("1. To one of my other accounts");
        System.out.println("2. To another customer's account");
        System.out.print("Choose destination type: ");
        String destinationChoice = scanner.nextLine();

        Account toAccount = null;
        Customer toCustomer = customer;
        boolean isOwnAccountTransfer = false;

        if ("1".equals(destinationChoice)) {
            toAccount = selectAccount(customer, "Transfer TO");
            if (toAccount == null) {
                return;
            }
            if (fromAccount.equals(toAccount)) {
                System.out.println("Transfer failed: Cannot transfer to the same source account.");
                return;
            }
            isOwnAccountTransfer = true;
        } else if ("2".equals(destinationChoice)) {

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
        } else {
            System.out.println("Invalid destination choice. Transfer cancelled.");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Error: Amount must be positive.");
                return;
            }

            String transactionType = "TRANSFER_OUT";
            double todayTransfers = fileService.calculateTodayUsage(customer, sourceAccountNumber, transactionType);
            double dailyTransferLimit;
            String limitType;

            if (isOwnAccountTransfer) {
                dailyTransferLimit = card.getOwnAccountTransferLimitPerDay();
                limitType = "Own Account Transfer Limit";
            } else {
                dailyTransferLimit = card.getExternalTransferLimitPerDay();
                limitType = "External Transfer Limit";
            }

            if (todayTransfers + amount > dailyTransferLimit) {
                System.out.printf("❌ Transaction Declined! Transfer exceeds the %s of $%.2f set by your %s card.%n",
                        limitType, dailyTransferLimit, card.getCardName());
                System.out.printf("   > Usage Today: $%.2f / Limit: $%.2f%n", todayTransfers, dailyTransferLimit);
                return;
            }

            fromAccount.transferMoney(fromAccount, toAccount, amount);

            fileService.updateCustomerRecord(customer);

            if (!customer.getUser_id().equals(toCustomer.getUser_id())) {
                fileService.updateCustomerRecord(toCustomer);
            }

            String fromDetails = String.format(
                    "\nDATE: %tF %tT\nTYPE: TRANSFER_OUT\nACCOUNT: %s\nAMOUNT: -%.2f\nNEW_BALANCE: %.2f\nTO_ACCOUNT: %s\n#####",
                    new Date(), new Date(),
                    sourceAccountNumber,
                    amount,
                    fromAccount.getBalance(),
                    toAccount.getAccount_number()
            );
            fileService.appendTransaction(customer, fromDetails);

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
            System.out.println("❌ Transfer failed: " + e.getMessage());
        }
    }
    public static void handleViewAccountStatement(Customer customer) {
        // 1. Get all accounts to display current balances
        List<Account> accounts = customer.getUserAccounts();

        if (accounts.isEmpty()) {
            System.out.println("You have no accounts to view a statement for.");
            return;
        }

        System.out.println("\n=======================================================");
        System.out.println("           🏦 DETAILED ACCOUNT STATEMENT 🏦           ");
        System.out.println("=======================================================");
        System.out.println("Customer: " + customer.getUser_name() + " (ID: " + customer.getUser_id() + ")");
        System.out.println("Report Date: " + String.format("%tF %tT", new Date(), new Date()));
        System.out.println("-----------------------------------------------------------------------------");

        for (Account acc : customer.getUserAccounts()) {

            String accountTypeDisplay;
            if (acc instanceof BankingAccount) {
                accountTypeDisplay = "Checking";
            } else if (acc instanceof SavingAccount) {
                accountTypeDisplay = "Saving";
            } else {
                accountTypeDisplay = "Unknown Type";
            }

            System.out.printf("Account: %s | Type: %s | CURRENT BALANCE: $%.2f%n",
                    acc.getAccount_number(),
                    accountTypeDisplay,
                    acc.getBalance()
            );
        }
        System.out.println("-----------------------------------------------------------------------------");

        List<TransactionRecord> transactions = fileService.parseTransactions(customer);

        if (transactions.isEmpty()) {
            System.out.println("\nNO TRANSACTION HISTORY FOUND.");
            System.out.println("=======================================================");
            return;
        }

        System.out.println("\t\t\t\tTransaction History:");
        System.out.printf("| %-20s | %-12s | %9s | %-35s | %11s |%n",
                "Date & Time", "Type", "Amount", "Notes", "New Balance");
        System.out.println("|----------------------|--------------|-----------|-------------------------------------|-------------|");

        for (TransactionRecord record : transactions) {
            System.out.println(record.toStatementRow());
        }

        System.out.println("\n========================================================================================");
    }

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

        if (filterType.equals("invalid")) {
            System.out.println("Invalid filter choice. Showing All Time transactions.");
            filterType = "all";
        }

        displayTransactionHistory(customer, filterType);
    }

    private static void displayTransactionHistory(Customer customer, String filterType) {
        List<TransactionRecord> transactions = fileService.filterTransactions(customer, filterType);

        System.out.println("\n=======================================================");
        System.out.println("           🧾 TRANSACTION HISTORY 🧾");
        System.out.println("Filter: " + filterType.toUpperCase());
        System.out.println("-------------------------------------------------------");

        if (transactions.isEmpty()) {
            System.out.println("NO TRANSACTION HISTORY FOUND matching the filter (" + filterType + ").");
        } else {
            System.out.printf("| %-20s | %-12s | %10s | %-35s | %11s |%n",
                    "Date & Time", "Type", "Amount", "Notes/Partner", "New Balance");
            System.out.println("|----------------------|--------------|------------|-------------------------------------|-------------|");

            for (TransactionRecord record : transactions) {
                System.out.println(record.toStatementRow());
            }
        }
        System.out.println("=======================================================\n");
    }

}
