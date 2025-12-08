package com.ga.project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit; // <-- ADD THIS IMPORT
import java.util.stream.Collectors; // <-- ADD THIS IMPORT

import static java.nio.file.Files.readAllLines;

public class FileService {
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static File myFile = new File("data.txt");
    private static final String TX_LOG_DIR = "transactions/"; // Directory for transaction logs
    private static final String DELIMITER = ";"; // The semicolon delimiter

    // Helper to ensure the transactions directory exists
    private static void ensureTxDirExists() {
        Path dir = Paths.get(TX_LOG_DIR);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir); // Creates directory and all necessary parent directories
            }
        } catch (IOException e) {
            System.err.println("Failed to create transaction directory: " + e.getMessage());
        }
    }

    private static DebitCard createCardInstanceByName(String name) {
        String cleanName = name.replace("Mastercard ", "").trim().toUpperCase();

        return switch (cleanName) {
            case "PLATINUM" -> new MastercardPlatinum();
            case "TITANIUM" -> new MastercardTitanium();
            // Handle cases where the card might just be saved as "Mastercard"
            case "MASTERCARD", "STANDARD" -> new Mastercard();
            default -> null; // Return null if name is unrecognized
        };
    }


    // --- READ/WRITE MAIN USER FILE ---

    public static List<User> readAllUsers() {
        List<User> allUsers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(myFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] parts = line.split(DELIMITER);

                    if (parts.length < 4) {
                        System.err.println("Skipping malformed line (too few fields before Role): " + line);
                        continue;
                    }

                    String id = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    String role = parts[3];

                    User user;

                    if (role.equalsIgnoreCase("Banker")) {
                        user = new Admin(id, username, password, role);

                    } else if (role.equalsIgnoreCase("Customer")) {

                        // EXPECT 7 FIELDS (ID, User, Pass, Role, Accounts, Balances, CardTypes)
                        if (parts.length != 7) { // <--- MODIFIED FROM 6 TO 7
                            System.err.println("Skipping malformed Customer line (expected 7 fields, got " + parts.length + "): " + line);
                            continue;
                        }

                        String accountsPart = parts[4].replaceAll("[\\[\\]\\s]", "");
                        String[] accountNumbers = accountsPart.split(",");

                        String balancesPart = parts[5].replaceAll("[\\[\\]\\s]", "");
                        String[] balancesStr = balancesPart.split(",");

                        // NEW: Parse card types (parts[6])
                        String cardsPart = parts[6].replaceAll("[\\[\\]\\s]", "");
                        String[] cardNames = cardsPart.split(",");


                        if (accountNumbers.length != balancesStr.length || accountNumbers.length != cardNames.length) { // <-- CHECK ADDED
                            System.err.println("Skipping malformed Customer line: Account/Balance/Card count mismatch. Line: " + line);
                            continue;
                        }

                        List<Account> accounts = new ArrayList<>();
                        for (int i = 0; i < accountNumbers.length; i++) {
                            String accNum = accountNumbers[i].trim();
                            double balance;
                            try {
                                balance = Double.parseDouble(balancesStr[i].trim());
                            } catch (NumberFormatException e) {
                                System.err.println("Failed to parse balance for account: " + accNum);
                                throw new RuntimeException("Data format error encountered.", e);
                            }

                            // NEW: Get the card instance
                            String cardName = cardNames[i].trim();
                            DebitCard linkedCard = createCardInstanceByName(cardName);
                            if (linkedCard == null) {
                                System.err.println("Unknown card type: " + cardName + ". Defaulting to Standard.");
                                linkedCard = new Mastercard();
                            }

                            Account account;
                            if (accNum.contains("BA")) {
                                account = new BankingAccount(accNum, linkedCard); // <-- UPDATED CONSTRUCTOR
                            } else if (accNum.contains("SA")) {
                                account = new SavingAccount(accNum, linkedCard); // <-- UPDATED CONSTRUCTOR
                            } else {
                                throw new IllegalArgumentException("Unknown account type: " + accNum);
                            }

                            accounts.add(account);
                        }

                        user = new Customer(id, username, password, role, accounts);

                    } else {
                        System.err.println("Skipping line with unknown role (" + role + "): " + line);
                        continue;
                    }

                    allUsers.add(user);

                } catch (Exception e) {
                    System.err.println("Fatal error during user parsing: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading all users: " + e.getMessage());
        }

        return allUsers;
    }

    /**
     * Saves a new customer record to the main data file and creates their default transaction log file.
     */
    public void saveToFile(Customer customer) {
        // 1. Ensure the transaction directory exists
        ensureTxDirExists();

        // Define the full path for the new customer's transaction log
        String txFileName = TX_LOG_DIR + "Customer-" + customer.getUser_name() + "-" + customer.getUser_id() + ".txt";

        // --- PART 1: Write to the main data.txt file ---
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(myFile, true))) {

            List<String> accounts = customer.getUserAccounts().stream()
                    .map(Account::getAccount_number).toList();

            List<Double> balances = customer.getUserAccounts().stream()
                    .map(Account::getBalance).toList();

            List<String> cardTypes = customer.getUserAccounts().stream()
                    .map(Account::getLinkedCardName).toList();

            writer.write(customer.getUser_id() + DELIMITER +
                    customer.getUser_name() + DELIMITER +
                    customer.getPassword() + DELIMITER +
                    customer.getRole() + DELIMITER +
                    accounts + DELIMITER +
                    balances+ DELIMITER +
                    cardTypes);
            writer.newLine();
            System.out.println("Data written to " + myFile);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        // --- PART 2: Create the customer's empty transaction file ---
        try {
            Path txFilePath = Paths.get(txFileName);
            if (!Files.exists(txFilePath)) {
                Files.createFile(txFilePath);
                System.out.println("Created default transaction file: " + txFileName);
            }
        } catch (IOException e) {
            System.err.println("Error creating default transaction file for " + customer.getUser_name() + ": " + e.getMessage());
        }
    }


    public void updateCustomerRecord(Customer customer) {
        try {
            List<String> lines = Files.readAllLines(Path.of("data.txt"));

            List<Account> accounts = customer.getUserAccounts();

            String newAccountNumbers = accounts.stream()
                    .map(Account::getAccount_number)
                    .collect(Collectors.joining(", "));

            String newBalances = accounts.stream()
                    .map(Account::getBalance)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            String newAccountsPart = "[" + newAccountNumbers + "]";
            String newBalancesPart = "[" + newBalances + "]";

            for (int i = 0; i < lines.size(); i++) {
                String currentLine = lines.get(i);
                String[] parts = currentLine.split(DELIMITER);

                if (parts.length > 1 && parts[1].equals(customer.getUser_name())) {

                    if (parts.length < 6 || !parts[3].equalsIgnoreCase("Customer")) {
                        continue;
                    }

                    parts[4] = newAccountsPart;
                    parts[5] = newBalancesPart;

                    lines.set(i, String.join(DELIMITER, parts));
                    break;
                }
            }

            Files.write(Path.of("data.txt"), lines);

        } catch (IOException e) {
            System.err.println("Error updating customer record for user " + customer.getUser_name() + ": " + e.getMessage());
        }
    }

    // --- LOOKUP METHODS ---

    public Account findAccountByNumber(String accountNumber) {
        List<User> allUsers = readAllUsers();

        for (User user : allUsers) {
            if (user instanceof Customer customer) {
                for (Account account : customer.getUserAccounts()) {
                    if (account.getAccount_number().equalsIgnoreCase(accountNumber)) {
                        return account;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the owner (Customer object) of a given Account by searching the master list.
     */
    public Customer findCustomerByAccount(Account targetAccount) {
        List<User> allUsers = readAllUsers();

        for (User user : allUsers) {
            if (user instanceof Customer customer) {
                boolean ownsAccount = customer.getUserAccounts().stream()
                        .anyMatch(acc -> acc.getAccount_number()
                                .equals(targetAccount.getAccount_number()));

                if (ownsAccount) {
                    return customer;
                }
            }
        }
        return null;
    }

    public User findByUsername(String username){
        List<User> current_users = readAllUsers();
        for (User customer: current_users){
            if(customer.getUser_name().equals(username))
                return customer;
        }
        return null;
    }

    // --- TRANSACTION LOGGING METHODS ---

    public void appendTransaction(Customer user, String transactionDetails) {
        ensureTxDirExists();
        String txFileName = TX_LOG_DIR + "Customer-" + user.getUser_name() + "-"+ user.getUser_id() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txFileName, true))) {
            // transactionDetails now contains the leading \n for visual break
            writer.write(transactionDetails);
            writer.newLine(); // Add newline after the log block for file structure integrity
        } catch (IOException e) {
            System.err.println("Error writing transaction log for user " + user.getUser_id() + ": " + e.getMessage());
        }
    }

    public static List<String> readTransactions(Customer user) {
        String txFileName = TX_LOG_DIR + "Customer-" + user.getUser_name() + "-"+ user.getUser_id() + ".txt";
        List<String> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(txFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                transactions.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No transaction history found for user " + user.getUser_name() + " (File exists but may be empty).");
        } catch (IOException e) {
            System.err.println("Error reading transaction log: " + e.getMessage());
        }
        return transactions;
    }

    public void updatePassword(String username, String newEncryptedPassword) {
        try {
            List<String> lines = readAllLines(Paths.get("data.txt"));

            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(";");

                if (parts[1].equals(username)) {
                    // parts[0] = userId
                    // parts[1] = username
                    // parts[2] = password → CHANGE ONLY THIS
                    parts[2] = newEncryptedPassword;

                    // Rebuild the line
                    lines.set(i, String.join(";", parts));
                    break;
                }
            }

            Files.write(Paths.get("data.txt"), lines);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static List<Customer> readAllCustomers() {
//        List<Customer> all_customers = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(myFile))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (!line.trim().isEmpty()) {
//                    String[] parts = line.split(",", 6);
//
//                    String userId = parts[0];
//                    String userName = parts[1];
//                    String password = parts[2];
//                    String role = parts[3];
//
//                    // Parse account numbers
//                    String accountsPart = parts[4].replaceAll("[\\[\\]\\s]", "");
//                    String[] accountNumbers = accountsPart.split(",");
//
//                    // Parse balances
//                    String balancesPart = parts[5].replaceAll("[\\[\\]\\s]", "");
//                    String[] balancesStr = balancesPart.split(",");
//
//                    List<Account> accounts = new ArrayList<>();
//                    for (int i = 0; i < accountNumbers.length; i++) {
//                        String accNum = accountNumbers[i];
//                        double balance = Double.parseDouble(balancesStr[i]);
//
//                        Account account;
//                        if (accNum.contains("BA")) {
//                            account = new BankingAccount(accNum, balance);
//                        } else if (accNum.contains("SA")) {
//                            account = new SavingAccount(accNum, balance);
//                        } else {
//                            // fallback if type unknown
//                            throw new IllegalArgumentException("Unknown account type: " + accNum);
//                        }
//
//                        accounts.add(account);
//                    }
//
//                    Customer customer = new Customer(userId, userName, password, role, accounts);
//                    all_customers.add(customer);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error reading all the users from file: " + e.getMessage());
//        }
//        return all_customers;
//    }
// find user from id
//    public Customer findById(String user_id){
//        List<Customer> current_users = readAllCustomers();
//        for (Customer customer: current_users){
//            if(customer.getUser_id().equals(user_id))
//                return customer;
//        }
//        return null;
//    }

// Inside FileService.java

    /**
     * Parses raw transaction log content into a list of structured TransactionRecord objects.
     */
    public List<TransactionRecord> parseTransactions(Customer customer) {
        List<String> rawContent = readTransactions(customer); // Use your existing reader
        String fullContent = String.join("\n", rawContent);
        List<TransactionRecord> records = new ArrayList<>();

        // 1. Split the content by the fixed record separator
        // Note: The log file might start with a blank line, resulting in an empty first element after the split.
        String[] blocks = fullContent.split("\n#####\n");

        for (String block : blocks) {
            String trimmedBlock = block.trim();
            if (trimmedBlock.isEmpty()) continue;

            TransactionRecord record = new TransactionRecord();
            String[] lines = trimmedBlock.split("\n");

            try {
                for (String line : lines) {
                    if (line.startsWith("DATE: ")) {
                        record.dateTime = line.substring("DATE: ".length()).trim();
                    } else if (line.startsWith("TYPE: ")) {
                        record.type = line.substring("TYPE: ".length()).trim();
                    } else if (line.startsWith("ACCOUNT: ")) {
                        record.account = line.substring("ACCOUNT: ".length()).trim();
                    } else if (line.startsWith("AMOUNT: ")) {
                        // Removes sign (+ or -) and parses the number, setting the sign in the double
                        String amountStr = line.substring("AMOUNT: ".length()).trim();
                        record.amount = Double.parseDouble(amountStr);
                    } else if (line.startsWith("NEW_BALANCE: ")) {
                        record.newBalance = Double.parseDouble(line.substring("NEW_BALANCE: ".length()).trim());
                    } else if (line.startsWith("NOTES: ")) {
                        record.notes = line.substring("NOTES: ".length()).trim();
                    } else if (line.startsWith("TO_ACCOUNT: ")) {
                        record.notes = "Transfer To: " + line.substring("TO_ACCOUNT: ".length()).trim();
                    } else if (line.startsWith("FROM_ACCOUNT: ")) {
                        record.notes = "Transfer From: " + line.substring("FROM_ACCOUNT: ".length()).trim();
                    }
                }
                // If the notes field is null (default for deposits/withdrawals without notes), set it to N/A
                if (record.notes == null) {
                    record.notes = "N/A";
                }
                records.add(record);
            } catch (Exception e) {
                System.err.println("Error parsing transaction block: " + trimmedBlock + ". Error: " + e.getMessage());
            }
        }
        return records;
    }

    /**
     * Parses raw transaction log content into a list of structured TransactionRecord objects.
     * * @param customer The customer whose transaction log is being read.
     * @return A list of parsed TransactionRecord objects.
     */
    public static List<TransactionRecord> getParsedStatement(Customer customer) {
        List<String> rawContent = readTransactions(customer);
        String fullContent = String.join("\n", rawContent);

        // 1. Initialize the list that will be returned (error fix)
        List<TransactionRecord> records = new ArrayList<>();

        // 2. Read and split blocks (error fix)
        String[] blocks = fullContent.split("\n#####\n");

        for (String block : blocks) {
            String trimmedBlock = block.trim();
            if (trimmedBlock.isEmpty()) continue;

            TransactionRecord record = new TransactionRecord();
            String[] lines = trimmedBlock.split("\n");

            try {
                for (String line : lines) {
                    if (line.startsWith("DATE: ")) {
                        record.dateTime = line.substring("DATE: ".length()).trim();
                        // Parse the date string into the LocalDateTime object
                        record.parsedDateTime = LocalDateTime.parse(record.dateTime, LOG_DATE_FORMATTER);
                    } else if (line.startsWith("TYPE: ")) {
                        record.type = line.substring("TYPE: ".length()).trim();
                    } else if (line.startsWith("ACCOUNT: ")) {
                        record.account = line.substring("ACCOUNT: ".length()).trim();
                    } else if (line.startsWith("AMOUNT: ")) {
                        // Parses the amount, keeping the sign (+/-)
                        String amountStr = line.substring("AMOUNT: ".length()).trim();
                        record.amount = Double.parseDouble(amountStr);
                    } else if (line.startsWith("NEW_BALANCE: ")) {
                        record.newBalance = Double.parseDouble(line.substring("NEW_BALANCE: ".length()).trim());
                    } else if (line.startsWith("NOTES: ")) {
                        record.notes = line.substring("NOTES: ".length()).trim();
                    } else if (line.startsWith("TO_ACCOUNT: ")) {
                        record.notes = "Transfer To: " + line.substring("TO_ACCOUNT: ".length()).trim();
                    } else if (line.startsWith("FROM_ACCOUNT: ")) {
                        record.notes = "Transfer From: " + line.substring("FROM_ACCOUNT: ".length()).trim();
                    }
                }

                // Final check for notes
                if (record.notes == null || record.notes.isEmpty() || record.notes.equalsIgnoreCase("N/A")) {
                    record.notes = "N/A";
                }

                // 3. Add the fully parsed record to the list (error fix)
                records.add(record);

            } catch (Exception e) {
                // In case of a single corrupt transaction block, log and skip it.
                System.err.println("Error parsing transaction block: " + trimmedBlock + ". Skipping this record.");
                e.printStackTrace();
            }
        }
        return records;
    }
//This method MUST be STATIC since it operates on Customer data retrieved via FileService methods.
    public static List<TransactionRecord> filterTransactions(Customer customer, String filterType) {
        // Get all parsed records. Assumes getParsedStatement is correctly defined elsewhere.
        List<TransactionRecord> allRecords = getParsedStatement(customer);

        // Define the current time for comparison
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime filterStart = null;
        LocalDateTime filterEnd = now; // Default end time is 'now' (inclusive)

        // Determine the start and end points for the filter based on the string condition
        switch (filterType.toLowerCase()) {
            case "today":
                filterStart = now.truncatedTo(ChronoUnit.DAYS); // Start of today (00:00:00)
                break;

            case "yesterday":
                // Start: Beginning of yesterday
                filterStart = now.minusDays(1).truncatedTo(ChronoUnit.DAYS); // 2025-12-07 00:00:00
                // End: Beginning of today (Excludes today's transactions)
                filterEnd = now.truncatedTo(ChronoUnit.DAYS); // 2025-12-08 00:00:00
                break;

            case "last week":
            case "last 7 days":
                // Go back 7 days from the beginning of today (to include all of today)
                filterStart = now.minusDays(7).truncatedTo(ChronoUnit.DAYS);
                break;

            case "last month":
            case "last 30 days":
                // Go back 30 days from the beginning of today
                filterStart = now.minusDays(30).truncatedTo(ChronoUnit.DAYS);
                break;

            case "all":
                return allRecords; // Return all without filtering

            default:
                System.err.println("Invalid filter type specified: " + filterType);
                return new ArrayList<>();
        }

        // CRITICAL: Handle the case where the switch defaulted or if filterStart wasn't set.
        if (filterStart == null) {
            System.err.println("Filtering error: filterStart was not initialized.");
            return new ArrayList<>();
        }

        // Capture the final boundaries for the filter stream
        final LocalDateTime finalFilterStart = filterStart;
        final LocalDateTime finalFilterEnd = filterEnd;

        // Filter the records using Java Streams
        return allRecords.stream()
                .filter(record -> record.parsedDateTime != null &&
                        // Check Start: Transaction date is NOT before the start date (>= start)
                        !record.parsedDateTime.isBefore(finalFilterStart) &&
                        // Check End: Transaction date IS before the end date (< end)
                        record.parsedDateTime.isBefore(finalFilterEnd))
                .collect(Collectors.toList());
    }

    public static double calculateTodayUsage(Customer customer, String accountNumber, String transactionType, String filterType) {
        // 1. Filter all records for the specified date range (should be "today")
        List<TransactionRecord> todayRecords = filterTransactions(customer, filterType);
        double totalUsage = 0.0;

        for (TransactionRecord record : todayRecords) {
            // 2. Check if the record matches the target account and type
            if (record.account.equals(accountNumber) &&
                    record.type.equalsIgnoreCase(transactionType)) {

                // 3. Sum the absolute amount (since we are tracking usage, not balance change)
                totalUsage += Math.abs(record.amount);
            }
        }
        return totalUsage;
    }


}